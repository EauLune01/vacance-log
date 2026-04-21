package vacance_log.sogang.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vacance_log.sogang.room.domain.Room;

public interface RoomRepository extends JpaRepository<Room,Long> {
}
