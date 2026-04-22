package vacance_log.sogang.photo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vacance_log.sogang.global.config.rabbitMq.RabbitMqConfig;
import vacance_log.sogang.global.exception.photoPlace.PhotoPlaceNotFoundException;
import vacance_log.sogang.global.exception.room.RoomNotFoundException;
import vacance_log.sogang.global.exception.user.UserNotFoundException;
import vacance_log.sogang.global.service.S3Service;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.photo.dto.command.PhotoUploadCommand;
import vacance_log.sogang.photo.dto.event.PhotoAnalysisEvent;
import vacance_log.sogang.photo.repository.PhotoRepository;
import vacance_log.sogang.place.domain.PhotoPlace;
import vacance_log.sogang.place.repository.PhotoPlaceRepository;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.room.repository.RoomRepository;
import vacance_log.sogang.user.domain.User;
import vacance_log.sogang.user.repository.UserRepository;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final PhotoPlaceRepository photoPlaceRepository;
    private final S3Service s3Service;
    private final RabbitTemplate rabbitTemplate;

    public void createPhoto(PhotoUploadCommand command) {
        // 1. 엔티티 존재 여부 검증
        Room room = getRoomOrThrow(command.getRoomId());
        User user = getUserOrThrow(command.getUserId());
        PhotoPlace photoPlace = getPhotoPlaceOrThrow(command.getPhotoPlaceId());

        // 2. S3 업로드 (경로: photos/{roomId}/{uuid_filename})
        String s3Url = s3Service.uploadPhoto(command.getFile(), command.getRoomId(), command.getUserId());

        // 3. Photo 엔티티 생성 및 저장
        Photo photo = Photo.createPhoto(s3Url, user, room, photoPlace);
        photoRepository.save(photo);

        // 4. LLM 비동기 메모 생성 이벤트 발행 (RabbitMQ)
        publishAIAnalysisEvent(photo.getId(), s3Url);
    }

    private void publishAIAnalysisEvent(Long photoId, String s3Url) {
        PhotoAnalysisEvent event = PhotoAnalysisEvent.of(photoId, s3Url);
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.PHOTO_EXCHANGE,
                RabbitMqConfig.PHOTO_UPLOADED,
                event
        );
        log.info("🚀 AI analysis event published: PhotoId: {}", photoId);
    }

    private Room getRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("방을 찾을 수 없습니다."));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));
    }

    private PhotoPlace getPhotoPlaceOrThrow(Long photoPlaceId) {
        return photoPlaceRepository.findById(photoPlaceId)
                .orElseThrow(() -> new PhotoPlaceNotFoundException("장소 정보를 찾을 수 없습니다."));
    }
}