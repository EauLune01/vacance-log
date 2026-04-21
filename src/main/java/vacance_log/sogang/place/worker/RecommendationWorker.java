package vacance_log.sogang.place.worker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vacance_log.sogang.global.config.rabbitMq.RabbitMqConfig;
import vacance_log.sogang.notification.domain.PushHistory;
import vacance_log.sogang.global.exception.room.RoomNotFoundException;
import vacance_log.sogang.notification.repository.PushHistoryRepository;
import vacance_log.sogang.notification.service.NotificationService;
import vacance_log.sogang.photo.repository.PhotoRepository;
import vacance_log.sogang.place.domain.PhotoPlace;
import vacance_log.sogang.place.dto.event.LocationEvent;
import vacance_log.sogang.place.repository.PhotoPlaceRepository;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.room.repository.RoomRepository;
import vacance_log.sogang.place.dto.event.PlaceCandidate;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationWorker {

    private final RoomRepository roomRepository;
    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final RecommendationPrompt promptStore;
    private final NotificationService notificationService;
    private final PhotoRepository photoRepository;
    private final PhotoPlaceRepository photoPlaceRepository;
    private final PushHistoryRepository pushHistoryRepository;

    @RabbitListener(queues = RabbitMqConfig.RECOMMENDATION_QUEUE)
    @Transactional
    public void handleLocationEvent(LocationEvent event) {
        try {
            Room room = getRoomOrThrow(event.getRoomId());
            String cityName = room.getCity().getName();

            // 1. Prepare context
            String combinedPersonas = extractMemberPersonas(room);
            String ragContext = fetchRagContext(room, cityName, event.getCandidates());

            // 2. Generate AI recommendation (Format: [PlaceCode] Message)
            String aiResponse = generateAiRecommendation(room, cityName, combinedPersonas, event.getCandidates(), ragContext);
            log.info("🤖 AI Response: {}", aiResponse);

            // 3. Parse and Dispatch (Save History & Send Notifications)
            processAndDispatch(room, cityName, aiResponse, event.getCandidates());

        } catch (Exception e) {
            log.error("❌ Error occurred while generating recommendation (roomId: {}): {}", event.getRoomId(), e.getMessage());
        }
    }

    private void processAndDispatch(Room room, String cityName, String aiResponse, List<PlaceCandidate> candidates) {
        String title = "📍 " + cityName + " Travel Guide";
        String placeCode;
        String content;

        try {
            placeCode = aiResponse.substring(aiResponse.indexOf("[") + 1, aiResponse.indexOf("]")).trim();
            content = aiResponse.substring(aiResponse.indexOf("]") + 1).trim();
        } catch (Exception e) {
            log.warn("⚠️ Failed to parse AI response, applying Fallback");
            placeCode = candidates.get(0).getPlaceCode();
            content = aiResponse;
        }

        //  4. Save to PushHistory
        savePushHistory(room, placeCode, content);

        final String finalPlaceCode = placeCode;
        final String finalContent = content;

        // 5. Send SSE Notifications
        room.getUserRooms().forEach(ur -> {
            try {
                notificationService.send(ur.getUser().getId(), title, finalContent, finalPlaceCode);
            } catch (Exception e) {
                log.warn("⚠️ Notification delivery failed (userId: {}): {}", ur.getUser().getId(), e.getMessage());
            }
        });
    }

    private void savePushHistory(Room room, String placeCode, String message) {
        try {
            PhotoPlace place = photoPlaceRepository.findByPlaceCode(placeCode)
                    .orElse(null);

            if (place != null) {
                PushHistory history = PushHistory.createHistory(room, place, message);
                pushHistoryRepository.save(history);
                log.info("💾 [History Saved] Recommended {} to Room {}", placeCode, room.getId());
            }
        } catch (Exception e) {
            log.error("❌ Failed to save PushHistory: {}", e.getMessage());
        }
    }

    private String extractMemberPersonas(Room room) {
        return room.getUserRooms().stream()
                .map(ur -> {
                    String nickname = ur.getUser().getNickname();
                    String personaEng = ur.getUser().getPersona().getEnglishName();
                    return String.format("%s(%s)", nickname, personaEng);
                })
                .collect(Collectors.joining(", "));
    }

    private String fetchRagContext(Room room, String cityName, List<PlaceCandidate> candidates) {
        // 1. [절대 제외] 이미 사진을 올린(방문 완료) 장소들
        Set<String> visitedCodes = photoRepository.findAllByRoom(room).stream()
                .map(Photo::getLandmarkName)
                .collect(Collectors.toSet());

        // 2. [우선순위 하락] 알림은 보냈지만 아직 안 간 장소들
        Set<String> pushedCodes = pushHistoryRepository.findAllByRoom(room).stream()
                .map(ph -> ph.getPhotoPlace().getPlaceCode())
                .collect(Collectors.toSet());

        // 3. 후보군 필터링 (일단 방문한 곳은 무조건 광탈)
        List<String> rawCandidates = candidates.stream()
                .map(PlaceCandidate::getPlaceCode)
                .filter(code -> !visitedCodes.contains(code))
                .toList();

        // 4. [핵심 로직] '완전 새로운 곳'이 있는지 확인
        List<String> freshCandidates = rawCandidates.stream()
                .filter(code -> !pushedCodes.contains(code))
                .toList();

        List<String> finalPlaceCodes;
        if (!freshCandidates.isEmpty()) {
            // 새로운 장소가 하나라도 있으면 새로운 곳들로만 RAG 구성 (신선함 유지)
            finalPlaceCodes = freshCandidates;
            log.info("✨ [RAG] Found {} fresh spots for room {}", freshCandidates.size(), room.getId());
        } else {
            // 새로운 곳이 없으면, 아까 추천했지만 안 간 곳이라도 다시 후보로 올림 (재추천)
            finalPlaceCodes = rawCandidates;
            log.info("🔄 [RAG] No fresh spots. Re-suggesting existing candidates for room {}", room.getId());
        }

        // 5. 최종 후보가 아예 없으면 (모든 주변 장소를 이미 방문함)
        if (finalPlaceCodes.isEmpty()) {
            log.warn("⚠️ [RAG] All candidates are already visited for room: {}", room.getId());
            return "The group has visited all nearby landmarks. Suggest a relaxing break or moving to a new area.";
        }

        // 6. 필터링된 장소들로 Vector Store 검색
        Filter.Expression filterExpression = new FilterExpressionBuilder()
                .in("placeCode", finalPlaceCodes.toArray())
                .build();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(cityName + " travel information")
                .filterExpression(filterExpression)
                .topK(5)
                .build();

        return vectorStore.similaritySearch(searchRequest).stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));
    }

    private String generateAiRecommendation(Room room, String cityName, String personas, List<PlaceCandidate> candidates, String context) {
        List<String> placeCodes = candidates.stream().map(PlaceCandidate::getPlaceCode).toList();
        int groupSize = room.getUserRooms().size();
        return chatClient.prompt()
                .system(sp -> sp.text(promptStore.getSystemPrompt()).param("city", cityName))
                .user(up -> up.text(promptStore.getUserPrompt())
                        .param("city", cityName)
                        .param("personas", personas)
                        .param("candidates", placeCodes.toString())
                        .param("context", context)
                        .param("group_size", groupSize))
                .call()
                .content();
    }

    private Room getRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found."));
    }
}