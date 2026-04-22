package vacance_log.sogang.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import vacance_log.sogang.diary.domain.DiaryType;
import vacance_log.sogang.global.exception.image.InvalidImageUrlException;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.place.dto.event.PlaceCandidate;
import vacance_log.sogang.place.worker.RecommendationPrompt;
import vacance_log.sogang.room.domain.Room;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final RecommendationPrompt promptStore;

    /**
     * 1. 장소 추천 로직
     */
    public String getRecommendation(Room room, String cityName, String personas, List<PlaceCandidate> candidates, String context) {
        List<String> placeCodes = candidates.stream()
                .map(PlaceCandidate::getPlaceCode)
                .toList();

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

    /**
     * 2. 사진별 AI 짧은 메모 생성
     */
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

    /**
     * 3. 최종 에세이(굿즈) 생성
     */
    public String generateFinalEssay(List<Photo> photos, DiaryType type) {
        String photoContext = photos.stream()
                .map(p -> String.format("[%s] %s", p.getCreatedAt().toLocalDate(), p.getDescription()))
                .collect(Collectors.joining("\n"));

        String systemInstruction = (type == DiaryType.INDIVIDUAL)
                ? "You are a sentimental travel writer. Write a 1st person essay based on the provided photo logs. Make it heart-warming."
                : "You are a cheerful group travel guide. Write a lively story for a group of friends. Use 'we' and focus on shared moments.";

        return chatClient.prompt()
                .system(systemInstruction)
                .user("Please write a travel diary in Korean based on these logs:\n" + photoContext)
                .call()
                .content();
    }

    /**
     * 4. 시맨틱 검색을 위한 벡터 생성
     */
    public float[] createEmbedding(String text) {
        try {
            return embeddingModel.embed(text);
        } catch (Exception e) {
            log.error("❌ [Embedding Failed] Error: {}", e.getMessage());
            return new float[1536];
        }
    }
}
