package vacance_log.sogang.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import vacance_log.sogang.rag.domain.TravelKnowledge;
import vacance_log.sogang.global.template.TravelPromptTemplates;
import vacance_log.sogang.global.exception.embedding.EmbeddingFailedException;
import vacance_log.sogang.global.exception.image.InvalidImageUrlException;
import vacance_log.sogang.global.exception.photoPlace.PhotoPlaceNotFoundException;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.place.dto.event.PlaceCandidate;
import vacance_log.sogang.place.repository.PhotoPlaceRepository;
import vacance_log.sogang.global.template.RecommendationPromptTemplates;
import vacance_log.sogang.rag.repository.TravelKnowledgeRepository;
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
    private final TravelKnowledgeRepository travelKnowledgeRepository;
    private final RecommendationPromptTemplates promptStore;

    public String getRecommendation(Room room, String cityName, String personas,
                                    List<PlaceCandidate> candidates, String userContext) {

        List<String> placeCodes = extractPlaceCodes(candidates);
        Map<String, String> placeNameMap = photoPlaceRepository.findPlaceNamesByCodes(placeCodes);
        String formattedCandidates = formatCandidates(candidates, placeNameMap);

        // 1. RAG 기반 검색
        String systemKnowledge = travelKnowledgeRepository.findByCodes(placeCodes).stream()
                .map(k -> String.format("[%s Tip]: %s", k.getPlaceCode(), k.getContent()))
                .collect(Collectors.joining("\n"));

        // 2. Hybrid Context 구성
        String hybridContext = String.format(
                "### User's Records\n%s\n\n### Professional Tips\n%s",
                (userContext != null) ? userContext : "None",
                systemKnowledge
        );

        int groupSize = room.getUserRooms().size();

        return chatClient.prompt()
                .system(sp -> sp.text(promptStore.getSystemPrompt()).param("city", cityName))
                .user(up -> up.text(promptStore.getUserPrompt())
                        .param("city", cityName)
                        .param("personas", personas)
                        .param("candidates", formattedCandidates)
                        .param("context", hybridContext)
                        .param("group_size", groupSize))
                .call()
                .content();
    }

    public String generateDetailedMemo(String imageUrl, String placeCode) {
        String knowledgeBase = travelKnowledgeRepository.findByCode(placeCode)
                .map(TravelKnowledge::getContent)
                .orElse("a meaningful travel spot");

        return chatClient.prompt()
                .user(u -> {
                    try {
                        u.text(String.format(TravelPromptTemplates.PHOTO_DESCRIPTION_INSTRUCTION, knowledgeBase))
                                .media(MimeTypeUtils.IMAGE_JPEG, URI.create(imageUrl).toURL());
                    } catch (MalformedURLException e) {
                        log.error("❌ [Vision Analysis Error] Invalid S3 URL: {}", imageUrl, e);
                        throw new InvalidImageUrlException("잘못된 이미지 URL입니다.");
                    }
                })
                .call()
                .content();
    }

    public String generateFinalEssay(List<Photo> photos) {
        String photoContext = photos.stream()
                .map(p -> String.format("[%s] %s", p.getCreatedAt().toLocalDate(), p.getDescription()))
                .collect(Collectors.joining("\n"));

        return chatClient.prompt()
                .system(TravelPromptTemplates.GROUP_DIARY_SYSTEM)
                .user(String.format(TravelPromptTemplates.DIARY_USER_INSTRUCTION, photoContext))
                .call()
                .content();
    }

    /**
     * 사용자 다이어리와 시스템 전문 지식을 결합하여 답변 생성 (Hybrid RAG)
     */
    public String generateAnswerFromDiaries(String query, String userContext, String systemKnowledge, String nickname) {

        // 1. 유저의 사적 기록과 시스템 지식을 구조적으로 결합
        StringBuilder combinedContext = new StringBuilder();

        combinedContext.append("### [User's Private Travel Records]\n")
                .append(userContext != null ? userContext : "No private records found.")
                .append("\n\n");

        if (systemKnowledge != null && !systemKnowledge.isBlank()) {
            combinedContext.append("### [Official Travel Knowledge & Tips]\n")
                    .append(systemKnowledge)
                    .append("\n");
        }

        // 2. 최종 프롬프트 실행
        return chatClient.prompt()
                .system(String.format(TravelPromptTemplates.RAG_ANSWER_SYSTEM, nickname))
                .user(String.format(TravelPromptTemplates.RAG_ANSWER_USER, combinedContext.toString(), query))
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
