package vacance_log.sogang.diary.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vacance_log.sogang.diary.domain.Diary;
import vacance_log.sogang.diary.domain.DiaryType;
import vacance_log.sogang.diary.repository.DiaryRepository;
import vacance_log.sogang.global.config.rabbitMq.RabbitMqConfig;
import vacance_log.sogang.global.exception.diary.DiaryNotFoundException;
import vacance_log.sogang.global.exception.room.RoomNotFoundException;
import vacance_log.sogang.global.service.OpenAiService;
import vacance_log.sogang.notification.service.NotificationService;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.photo.repository.PhotoRepository;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.room.repository.RoomRepository;
import vacance_log.sogang.user.domain.User;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class DiaryConsumer {
    private final RoomRepository roomRepository;
    private final PhotoRepository photoRepository;
    private final DiaryRepository diaryRepository;
    private final OpenAiService openAiService;
    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMqConfig.DIARY_GENERATE_QUEUE)
    @Transactional
    public void handleDiaryGeneration(Long roomId) {
        log.info("📩 [Async Start] Generating diaries for Room: {}", roomId);

        Room room = roomRepository.findByIdWithUserRooms(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found."));

        // 1. 개인 다이어리 업데이트
        room.getUserRooms().forEach(ur -> updateIndividualDiaryContent(room, ur.getUser()));

        // 2. 그룹 다이어리 생성
        createGroupDiary(room);

        // 3. 완료 알림 발송
        sendDiaryCompletionNotification(room);

        log.info("✅ [Async End] Diaries completed for Room: {}", roomId);
    }

    private void sendDiaryCompletionNotification(Room room) {
        log.info("🔔 [Notification] Triggering diary alerts for Room: {}", room.getTitle());
        room.getUserRooms().forEach(ur ->
                notificationService.sendDiaryNotification(ur.getUser().getId(), room.getTitle())
        );
    }

    // 1. 개인 다이어리 업데이트
    private void updateIndividualDiaryContent(Room room, User user) {
        List<Photo> photos = photoRepository.findAllByRoomAndUser(room, user);

        if (photos.isEmpty()) {
            log.warn("⚠️ No photos found for user: {} in room: {}", user.getId(), room.getId());
            return;
        }

        String essay = openAiService.generateFinalEssay(photos, DiaryType.INDIVIDUAL);
        float[] embedding = openAiService.createEmbedding(essay);

        Diary diary = diaryRepository.findByRoomAndUserAndType(room, user, DiaryType.INDIVIDUAL)
                .orElseThrow(() -> new DiaryNotFoundException("Individual diary shell not found."));

        diary.updateContent(essay);
        diary.updateEmbedding(embedding);
    }

    // 2. 그룹 다이어리 생성
    private void createGroupDiary(Room room) {
        List<Photo> allPhotos = photoRepository.findAllByRoom(room);
        if (allPhotos.isEmpty()) return;

        String groupEssay = openAiService.generateFinalEssay(allPhotos, DiaryType.GROUP);
        float[] embedding = openAiService.createEmbedding(groupEssay);

        Diary groupDiary = Diary.createEssay(room);
        groupDiary.updateContent(groupEssay);
        groupDiary.updateEmbedding(embedding);

        diaryRepository.save(groupDiary);
    }
}