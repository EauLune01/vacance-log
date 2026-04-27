package vacance_log.sogang.place.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vacance_log.sogang.global.config.rabbitMq.RabbitMqConfig;
import vacance_log.sogang.global.service.OpenAiService;
import vacance_log.sogang.notification.domain.PushHistory;
import vacance_log.sogang.global.exception.room.RoomNotFoundException;
import vacance_log.sogang.notification.repository.PushHistoryRepository;
import vacance_log.sogang.notification.service.NotificationService;
import vacance_log.sogang.photo.repository.PhotoRepository;
import vacance_log.sogang.place.domain.PhotoPlace;
import vacance_log.sogang.place.dto.event.LocationEvent;
import vacance_log.sogang.place.repository.PhotoPlaceRepository;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.room.repository.RoomRepository;
import vacance_log.sogang.place.dto.event.PlaceCandidate;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationConsumer {

    private final RoomRepository roomRepository;
    private final VectorStore vectorStore;
    private final NotificationService notificationService;
    private final PhotoRepository photoRepository;
    private final PhotoPlaceRepository photoPlaceRepository;
    private final PushHistoryRepository pushHistoryRepository;
    private final OpenAiService openAiService;

    /**
     * RabbitMQ로부터 위치 감지 이벤트를 수신하여 AI 추천을 처리합니다.
     */
    @RabbitListener(queues = RabbitMqConfig.RECOMMENDATION_QUEUE)
    @Transactional
    public void handleLocationEvent(LocationEvent event) {
        try {
            Room room = getRoomOrThrow(event.getRoomId());
            String cityName = room.getCity().getName();

            // 1. 추천을 위한 컨텍스트 준비 (멤버 페르소나 및 RAG 데이터)
            String combinedPersonas = extractMemberPersonas(room);
            String ragContext = fetchRagContext(room, cityName, event.getCandidates());

            // 2. OpenAI를 통한 추천 메시지 생성 (포맷: [PlaceCode] 메시지)
            String aiResponse = openAiService.getRecommendation(room, cityName, combinedPersonas, event.getCandidates(), ragContext);
            log.info("🤖 AI 추천 응답: {}", aiResponse);

            // 3. 파싱 및 배포 (히스토리 저장 및 알림 발송)
            processAndDispatch(room, cityName, aiResponse, event.getCandidates());

        } catch (Exception e) {
            log.error("❌ 추천 생성 중 오류 발생 (roomId: {}): {}", event.getRoomId(), e.getMessage());
        }
    }

    /**
     * AI 응답을 파싱하여 DB에 저장하고 사용자들에게 알림을 보냅니다.
     */
    private void processAndDispatch(Room room, String cityName, String aiResponse, List<PlaceCandidate> candidates) {
        String title = "📍 " + cityName + " 여행 가이드";
        String placeCode;
        String content;

        try {
            placeCode = aiResponse.substring(aiResponse.indexOf("[") + 1, aiResponse.indexOf("]")).trim();
            content = aiResponse.substring(aiResponse.indexOf("]") + 1).trim();
        } catch (Exception e) {
            log.warn("⚠️ AI 응답 파싱 실패, 기본 후보지로 폴백 처리");
            placeCode = candidates.get(0).getPlaceCode();
            content = aiResponse;
        }

        String toFinalPlaceCode = placeCode;
        PhotoPlace photoPlace = photoPlaceRepository
                .findByCityIdAndPlaceCode(room.getCity().getId(), placeCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "존재하지 않는 PhotoPlace입니다. cityId="
                                + room.getCity().getId()
                                + ", placeCode="
                                + toFinalPlaceCode
                ));

        Long photoPlaceId = photoPlace.getId();

        savePushHistory(room, placeCode, content);

        final String finalPlaceCode = placeCode;
        final String finalContent = content;
        final Long finalPhotoPlaceId = photoPlaceId;

        room.getUserRooms().forEach(ur -> {
            try {
                notificationService.sendPlaceRecommendation(
                        ur.getUser().getId(),
                        room.getId(),
                        title,
                        finalContent,
                        finalPlaceCode,
                        finalPhotoPlaceId
                );
            } catch (Exception e) {
                log.warn("⚠️ 알림 전송 실패 (userId: {}): {}", ur.getUser().getId(), e.getMessage());
            }
        });
    }

    /**
     * 추천된 장소 정보를 PushHistory 테이블에 기록합니다.
     */
    private void savePushHistory(Room room, String placeCode, String message) {
        try {
            PhotoPlace place = photoPlaceRepository.findByPlaceCode(placeCode)
                    .orElse(null);

            if (place != null) {
                PushHistory history = PushHistory.createHistory(room, place, message);
                pushHistoryRepository.save(history);
                log.info("💾 [히스토리 저장] 장소: {}, 방 ID: {}", placeCode, room.getId());
            }
        } catch (Exception e) {
            log.error("❌ PushHistory 저장 실패: {}", e.getMessage());
        }
    }

    /**
     * 방에 참여 중인 모든 멤버의 페르소나 정보를 추출합니다.
     */
    private String extractMemberPersonas(Room room) {
        return room.getUserRooms().stream()
                .map(ur -> {
                    String nickname = ur.getUser().getNickname();
                    String personaEng = ur.getUser().getPersona().getEnglishName();
                    return String.format("%s(%s)", nickname, personaEng);
                })
                .collect(Collectors.joining(", "));
    }

    /**
     * 방문 여부와 추천 이력을 고려하여 최적의 RAG 컨텍스트를 구성합니다.
     */
    private String fetchRagContext(Room room, String cityName, List<PlaceCandidate> candidates) {
        // 1. [제외 대상] 이미 사진을 업로드한(방문 완료) 장소 추출
        Set<String> visitedCodes = photoRepository.findAllByRoom(room).stream()
                .filter(photo -> photo.getPhotoPlace() != null)
                .map(photo -> photo.getPhotoPlace().getPlaceCode())
                .collect(Collectors.toSet());

        // 2. [우선순위 낮음] 이전에 알림을 보냈던 장소 추출
        Set<String> pushedCodes = pushHistoryRepository.findAllByRoom(room).stream()
                .map(ph -> ph.getPhotoPlace().getPlaceCode())
                .collect(Collectors.toSet());

        // 3. 후보군 필터링 (방문한 곳은 무조건 제외)
        List<String> rawCandidates = candidates.stream()
                .map(PlaceCandidate::getPlaceCode)
                .filter(code -> !visitedCodes.contains(code))
                .toList();

        // 4. '완전 새로운 장소' 필터링 (알림도 보낸 적 없는 곳)
        List<String> freshCandidates = rawCandidates.stream()
                .filter(code -> !pushedCodes.contains(code))
                .toList();

        List<String> finalPlaceCodes;
        if (!freshCandidates.isEmpty()) {
            // 새로운 장소가 있다면 신선함 유지를 위해 해당 장소들로만 RAG 구성
            finalPlaceCodes = freshCandidates;
            log.info("✨ [RAG] 새로운 장소 {}건 발견 (방 ID: {})", freshCandidates.size(), room.getId());
        } else {
            // 새로운 곳이 없다면 이전에 추천했지만 안 간 곳이라도 재추천 후보로 사용
            finalPlaceCodes = rawCandidates;
            log.info("🔄 [RAG] 새로운 장소 없음. 기존 후보 재추천 진행 (방 ID: {})", room.getId());
        }

        // 5. 모든 주변 장소를 방문한 경우 예외 처리
        if (finalPlaceCodes.isEmpty()) {
            log.warn("⚠️ [RAG] 주변 모든 후보지를 이미 방문함 (방 ID: {})", room.getId());
            return "그룹이 주변 모든 랜드마크를 방문했습니다. 휴식을 취하거나 새로운 지역으로 이동할 것을 권장하세요.";
        }

        // 6. 필터링된 장소 코드를 기반으로 벡터 스토어 유사도 검색 수행
        Filter.Expression filterExpression = new FilterExpressionBuilder()
                .in("placeCode", finalPlaceCodes.toArray())
                .build();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(cityName + " 여행 정보")
                .filterExpression(filterExpression)
                .topK(5)
                .build();

        return vectorStore.similaritySearch(searchRequest).stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));
    }

    private Room getRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("방을 찾을 수 없습니다."));
    }
}