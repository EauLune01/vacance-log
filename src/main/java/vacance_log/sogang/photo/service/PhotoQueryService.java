package vacance_log.sogang.photo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vacance_log.sogang.global.exception.room.RoomNotFoundException;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.photo.dto.common.UserPhoto;
import vacance_log.sogang.photo.dto.result.PhotoStatusResult;
import vacance_log.sogang.photo.repository.PhotoRepository;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.room.repository.RoomRepository;
import vacance_log.sogang.user.domain.User;

import java.util.Map;
import java.util.stream.Collectors;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PhotoQueryService {
    private final PhotoRepository photoRepository;
    private final RoomRepository roomRepository;

    public PhotoStatusResult getPhotoStatus(Long roomId, Long photoPlaceId) {
        Room room = getRoomOrThrow(roomId);

        List<User> participants = room.getParticipants();
        List<Photo> photos = photoRepository.findPhotosByPlace(roomId, photoPlaceId);

        Map<Long, Photo> photoMap = photos.stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), p -> p));

        List<UserPhoto> userPhotos = participants.stream()
                .map(user -> {
                    Photo p = photoMap.get(user.getId());
                    return UserPhoto.of(
                            user.getId(),
                            user.getNickname(),
                            p != null ? p.getS3Url() : null,
                            p != null
                    );
                }).toList();

        return PhotoStatusResult.of(
                userPhotos,
                participants.size(),
                photos.size(),
                participants.size() == photos.size()
        );
    }

    private Room getRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found."));
    }
}
