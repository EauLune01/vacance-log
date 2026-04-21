package vacance_log.sogang.photo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.room.domain.Room;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo,Long> {
    List<Photo> findAllByRoom(Room room);
}
