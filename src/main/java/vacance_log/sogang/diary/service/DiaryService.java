package vacance_log.sogang.diary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vacance_log.sogang.diary.domain.DiaryType;
import vacance_log.sogang.diary.dto.command.DiaryQueryCommand;
import vacance_log.sogang.diary.dto.result.DiaryDetailResult;
import vacance_log.sogang.diary.repository.DiaryRepository;
import vacance_log.sogang.global.exception.diary.DiaryNotFoundException;
import vacance_log.sogang.global.exception.room.RoomNotFoundException;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.photo.repository.PhotoRepository;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.room.repository.RoomRepository;
import vacance_log.sogang.user.domain.User;
import vacance_log.sogang.user.repository.UserRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final RoomRepository roomRepository;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;

    public DiaryDetailResult getDiaryDetail(DiaryQueryCommand command) {
        Room room = getRoomOrThrow(command.getRoomId());
        User user = (command.getType() == DiaryType.INDIVIDUAL && command.getUserId() != null)
                ? userRepository.getReferenceById(command.getUserId())
                : null;

        String content = findDiaryContent(room, command.getType(), user);
        List<String> imageUrls = findImageUrls(room, command.getType(), user);

        return DiaryDetailResult.of(
                room.getTitle(),
                content,
                room.getCity().getName(),
                imageUrls
        );
    }

    private String findDiaryContent(Room room, DiaryType type, User user) {
        return diaryRepository.findContent(room, type, user)
                .orElseThrow(() -> new DiaryNotFoundException("해당 다이어리를 찾을 수 없습니다."));
    }

    private List<String> findImageUrls(Room room, DiaryType type, User user) {
        List<Photo> photos = (type == DiaryType.INDIVIDUAL && user != null)
                ? photoRepository.findAllByRoomAndUser(room, user)
                : photoRepository.findAllByRoom(room);

        return photos.stream()
                .map(Photo::getS3Url)
                .toList();
    }

    private Room getRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found."));
    }
}