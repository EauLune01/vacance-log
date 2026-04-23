package vacance_log.sogang.room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vacance_log.sogang.room.domain.UserRoom;
import java.util.List;
public interface UserRoomRepository extends JpaRepository<UserRoom,Long> {
    List<UserRoom> findAllByUserId(Long userId);
}
