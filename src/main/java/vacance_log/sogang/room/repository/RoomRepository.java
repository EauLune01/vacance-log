package vacance_log.sogang.room.repository;

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vacance_log.sogang.room.domain.Room;

import java.lang.annotation.Annotation;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room,Long> {
    @Query("SELECT r FROM Room r " +
            "JOIN FETCH r.userRooms " +
            "JOIN FETCH r.userRooms.user " +
            "WHERE r.id = :roomId")
    Optional<Room> findByIdWithUserRooms(@Param("roomId") Long roomId);

    Optional<Room> findByTitle(String romanticParisTrip);
}
