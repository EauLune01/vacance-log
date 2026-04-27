package vacance_log.sogang.photo.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vacance_log.sogang.global.exception.photo.PhotoNotFoundException;
import vacance_log.sogang.global.service.OpenAiService;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.photo.dto.event.PhotoAnalysisEvent;
import vacance_log.sogang.photo.repository.PhotoRepository;
import vacance_log.sogang.photo.service.PhotoVectorService;
import vacance_log.sogang.room.domain.Room;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoAnalysisConsumer {

    private final PhotoRepository photoRepository;
    private final OpenAiService openAiService;
    private final PhotoVectorService photoVectorService;

    @RabbitListener(queues = "photo.analysis.queue")
    @Transactional
    public void handlePhotoAnalysis(PhotoAnalysisEvent event) {
        log.info("📩 [Async Analysis] Event received - PhotoId: {}", event.getPhotoId());

        // 1. Photo 엔티티 조회
        Photo photo = photoRepository.findByIdDetail(event.getPhotoId())
                .orElseThrow(() -> {
                    log.error("❌ [Extraction Failed] Photo not found for ID: {}", event.getPhotoId());
                    return new PhotoNotFoundException("Photo not found.");
                });

        Room room = photo.getRoom();
        String placeCode = (photo.getPhotoPlace() != null) ? photo.getPhotoPlace().getPlaceCode() : null;

        try {
            // 2. GPT-4o Vision 분석: 2~3문장의 풍부한 설명 생성
            String aiMemo = openAiService.generateDetailedMemo(photo.getS3Url(), placeCode);
            photo.updateDescription(aiMemo);

            // 3. 🚀 [Photo RAG] 사진 개별 메모 벡터 DB 저장
            photoVectorService.upsert(photo, aiMemo);

            log.info("✅ [Analysis & RAG Success] PhotoId: {} -> Memo: {}", photo.getId(), aiMemo);

        } catch (Exception e) {
            log.warn("⚠️ [Analysis Failed] GPT Error for PhotoId: {}. error={}",
                    event.getPhotoId(), e.getMessage());
            photo.updateDescription("A meaningful moment in " + room.getCity().getName());
        }

        log.info("🏁 [Task Completed] Consumer logic finished for PhotoId: {}", photo.getId());
    }
}
