package vacance_log.sogang.photo.repository.custom;

import vacance_log.sogang.photo.domain.Photo;
import java.util.List;
import java.util.Optional;

public interface PhotoRepositoryCustom {
    List<Photo> findPhotosByPlace(Long roomId, Long photoPlaceId);
    Optional<Photo> findByIdWithRoomAndUser(Long photoId);
}
