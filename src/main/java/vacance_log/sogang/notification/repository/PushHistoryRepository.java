package vacance_log.sogang.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vacance_log.sogang.notification.domain.PushHistory;
import vacance_log.sogang.room.domain.Room;

import java.util.List;

public interface PushHistoryRepository extends JpaRepository<PushHistory, Long> {
    List<PushHistory> findAllByRoom(Room room);
}
