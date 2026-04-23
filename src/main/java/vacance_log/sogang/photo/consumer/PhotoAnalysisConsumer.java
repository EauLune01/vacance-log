package vacance_log.sogang.photo.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vacance_log.sogang.diary.domain.Diary;
import vacance_log.sogang.diary.domain.DiaryType;
import vacance_log.sogang.diary.repository.DiaryRepository;
import vacance_log.sogang.global.exception.photo.PhotoNotFoundException;
import vacance_log.sogang.global.service.OpenAiService;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.photo.dto.event.PhotoAnalysisEvent;
import vacance_log.sogang.photo.repository.PhotoRepository;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.user.domain.User;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoAnalysisConsumer {

    private final PhotoRepository photoRepository;
    private final DiaryRepository diaryRepository;
    private final OpenAiService openAiService;

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
        User user = photo.getUser();

        String placeCode = (photo.getPhotoPlace() != null) ? photo.getPhotoPlace().getPlaceCode() : null;

        try {
            // 2. GPT-4o Vision 분석: 장소 정보(RAG)를 포함하여 짧은 시적 메모 생성
            String aiMemo = openAiService.generateShortMemo(photo.getS3Url(), placeCode);
            photo.updateDescription(aiMemo);

            log.info("✅ [Analysis Success] PhotoId: {} -> Memo: {}", photo.getId(), aiMemo);

        } catch (Exception e) {
            log.warn("⚠️ [Analysis Failed] GPT Error for PhotoId: {}. Falling back to default. error={}",
                    event.getPhotoId(), e.getMessage());
            photo.updateDescription("Memorable moment in " + room.getCity().getName());
        }

        // 3. 🏛️ 개인 다이어리(굿즈) 선제 생성
        ensureIndividualDiaryExists(room, user);

        log.info("🏁 [Task Completed] Consumer logic finished for PhotoId: {}", photo.getId());
    }

    private void ensureIndividualDiaryExists(Room room, User user) {
        diaryRepository.findByRoomAndUserAndType(room, user, DiaryType.INDIVIDUAL)
                .ifPresentOrElse(
                        diary -> log.info("✨ [Diary Check] Already exists for User: {}", user.getNickname()),
                        () -> {
                            log.info("✨ [Diary Creation] First upload detected. Initializing Diary for User: {}", user.getNickname());
                            diaryRepository.save(Diary.createPersonal(room, user));
                        }
                );
    }
}
