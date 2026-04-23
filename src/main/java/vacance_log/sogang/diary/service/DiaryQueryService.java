package vacance_log.sogang.diary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vacance_log.sogang.diary.domain.DiaryType;
import vacance_log.sogang.diary.dto.command.DiaryQueryCommand;
import vacance_log.sogang.diary.dto.result.DiaryDetailResult;
import vacance_log.sogang.diary.dto.result.GroupDiaryResult;
import vacance_log.sogang.diary.dto.result.PersonalDiaryResult;
import vacance_log.sogang.diary.repository.DiaryRepository;
import vacance_log.sogang.global.exception.diary.DiaryNotFoundException;
import vacance_log.sogang.global.exception.room.RoomNotFoundException;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.photo.dto.result.PhotoInfo;
import vacance_log.sogang.photo.repository.PhotoRepository;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.room.repository.RoomRepository;
import vacance_log.sogang.user.domain.User;
import vacance_log.sogang.user.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DiaryQueryService {

    private final DiaryRepository diaryRepository;
    private final RoomRepository roomRepository;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;

    public DiaryDetailResult getGroupDiary(Long roomId) {
        Room room = getRoomOrThrow(roomId);

        String content = diaryRepository.findContent(room, DiaryType.GROUP, null)
                .orElseThrow(() -> new DiaryNotFoundException("그룹 다이어리가 생성되지 않았습니다."));

        List<String> imageUrls = photoRepository.findAllByRoom(room).stream()
                .map(Photo::getS3Url)
                .toList();

        // GroupDiaryResult는 DiaryDetailResult를 구현하고 있어야 함
        return GroupDiaryResult.of(
                room.getTitle(),
                content,
                room.getCity().getName(),
                imageUrls
        );
    }

    public DiaryDetailResult getPersonalDiary(DiaryQueryCommand command) {
        Room room = getRoomOrThrow(command.getRoomId());
        User user = userRepository.getReferenceById(command.getUserId());

        List<Photo> photos = photoRepository.findAllByRoomAndUser(room, user);

        if (photos.isEmpty()) {
            throw new DiaryNotFoundException("해당 유저의 여행 기록이 없습니다.");
        }

        List<PhotoInfo> photoInfos = photos.stream()
                .map(PhotoInfo::from)
                .toList();

        return PersonalDiaryResult.of(room.getTitle(), room.getCity().getName(), photoInfos);
    }

    private Room getRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found."));
    }
}