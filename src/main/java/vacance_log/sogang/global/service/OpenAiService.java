package vacance_log.sogang.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import vacance_log.sogang.diary.domain.DiaryType;
import vacance_log.sogang.diary.template.TravelPromptTemplates;
import vacance_log.sogang.global.exception.embedding.EmbeddingFailedException;
import vacance_log.sogang.global.exception.image.InvalidImageUrlException;
import vacance_log.sogang.global.exception.photoPlace.PhotoPlaceNotFoundException;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.place.dto.event.PlaceCandidate;
import vacance_log.sogang.place.repository.PhotoPlaceRepository;
import vacance_log.sogang.place.worker.RecommendationPrompt;
import vacance_log.sogang.room.domain.Room;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final PhotoPlaceRepository photoPlaceRepository;
    private final RecommendationPrompt promptStore;

    public String getRecommendation(Room room, String cityName, String personas, List<PlaceCandidate> candidates, String context) {

        List<String> placeCodes = extractPlaceCodes(candidates);

        Map<String, String> placeNameMap = photoPlaceRepository.findPlaceNamesByCodes(placeCodes);

        String formattedCandidates = formatCandidates(candidates, placeNameMap);

        int groupSize = room.getUserRooms().size();

        return chatClient.prompt()
                .system(sp -> sp.text(promptStore.getSystemPrompt())
                        .param("city", cityName))
                .user(up -> up.text(promptStore.getUserPrompt())
                        .param("city", cityName)
                        .param("personas", personas)
                        .param("candidates", formattedCandidates)
                        .param("context", context)
                        .param("group_size", groupSize))
                .call()
                .content();
    }

    public String generateShortMemo(String imageUrl) {
        return chatClient.prompt()
                .user(u -> {
                    try {
                        u.text("Write a poetic English caption. MAX 20 BYTES. Just 3~5 words.")
                                .media(MimeTypeUtils.IMAGE_JPEG, URI.create(imageUrl).toURL());
                    } catch (MalformedURLException e) {
                        log.error("❌ Invalid S3 URL: {}", imageUrl);
                        throw new InvalidImageUrlException("잘못된 이미지 URL입니다.");
                    }
                })
                .call()
                .content();
    }

    public String generateFinalEssay(List<Photo> photos, DiaryType type) {
        String photoContext = photos.stream()
                .map(p -> String.format("[%s] %s", p.getCreatedAt().toLocalDate(), p.getDescription()))
                .collect(Collectors.joining("\n"));

        String systemInstruction = (type == DiaryType.INDIVIDUAL)
                ? TravelPromptTemplates.INDIVIDUAL_DIARY_SYSTEM
                : TravelPromptTemplates.GROUP_DIARY_SYSTEM;

        return chatClient.prompt()
                .system(systemInstruction)
                .user(String.format(TravelPromptTemplates.DIARY_USER_INSTRUCTION, photoContext))
                .call()
                .content();
    }

    public String generateAnswerFromDiaries(String query, String context, String nickname) {
        return chatClient.prompt()
                .system(String.format(TravelPromptTemplates.RAG_ANSWER_SYSTEM, nickname))
                .user(String.format(TravelPromptTemplates.RAG_ANSWER_USER, context, query))
                .call()
                .content();
    }

    public String extractSearchKeywords(String rawQuery) {
        try {
            String keywords = chatClient.prompt()
                    .system("Extract only 1~3 core search keywords (location, activity, or specific items) from the user's travel-related question. Return ONLY the keywords separated by spaces. If no specific keyword is found, return an empty string.")
                    .user(rawQuery)
                    .call()
                    .content();

            return (keywords != null) ? keywords.trim() : "";
        } catch (Exception e) {
            log.warn("⚠️ [Keyword Extraction Failed] Fallback to original query. error={}", e.getMessage());
            return "";
        }
    }

    public float[] createEmbedding(String text) {
        try {
            return embeddingModel.embed(text);
        } catch (Exception e) {
            log.error("❌ [Embedding Failed] text={}, error={}", text, e.getMessage(), e);
            throw new EmbeddingFailedException("Embedding 생성에 실패했습니다.");
        }
    }

    private List<String> extractPlaceCodes(List<PlaceCandidate> candidates) {
        return candidates.stream()
                .map(PlaceCandidate::getPlaceCode)
                .toList();
    }

    private String formatCandidates(List<PlaceCandidate> candidates, Map<String, String> placeNameMap) {
        return candidates.stream()
                .map(c -> formatCandidate(c, placeNameMap))
                .collect(Collectors.joining("\n"));
    }

    private String formatCandidate(PlaceCandidate candidate, Map<String, String> placeNameMap) {
        String code = candidate.getPlaceCode();

        String placeName = placeNameMap.get(code);
        if (placeName == null) {
            throw new PhotoPlaceNotFoundException("❌ placeName not found.");
        }

        return String.format("- %s (%s)", code, placeName);
    }
}
