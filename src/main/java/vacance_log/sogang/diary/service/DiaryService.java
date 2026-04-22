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
        String content = findDiaryContent(room, command);
        List<String> imageUrls = findImageUrls(room, command);

        return DiaryDetailResult.of(
                room.getTitle(),
                content,
                room.getCity().getName(),
                imageUrls
        );
    }

    private String findDiaryContent(Room room, DiaryQueryCommand command) {
        User user = (command.getType() == DiaryType.INDIVIDUAL)
                ? userRepository.getReferenceById(command.getUserId())
                : null;

        return diaryRepository.findContent(room, command.getType(), user)
                .orElseThrow(() -> new DiaryNotFoundException("해당 다이어리를 찾을 수 없습니다."));
    }


    private List<String> findImageUrls(Room room, DiaryQueryCommand command) {
        List<Photo> photos = (command.getType() == DiaryType.INDIVIDUAL)
                ? photoRepository.findAllByRoomAndUser(room, userRepository.getReferenceById(command.getUserId()))
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