package vacance_log.sogang.photo.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.photo.repository.custom.PhotoRepositoryCustom;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.user.domain.User;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo,Long>, PhotoRepositoryCustom {
    @EntityGraph(attributePaths = {"user", "room"})
    List<Photo> findAllByRoomAndUser(Room room, User user);

    @EntityGraph(attributePaths = {"room"})
    List<Photo> findAllByRoom(Room room);

}
