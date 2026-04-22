package vacance_log.sogang.diary.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vacance_log.sogang.diary.domain.Diary;
import vacance_log.sogang.diary.domain.DiaryType;
import vacance_log.sogang.diary.repository.custom.DiaryRepositoryCustom;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.user.domain.User;

import java.util.Optional;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long>, DiaryRepositoryCustom {
    boolean existsByRoomAndUserAndType(Room room, User user, DiaryType type);

    @EntityGraph(attributePaths = {"room", "user"})
    Optional<Diary> findByRoomAndUserAndType(Room room, User user, DiaryType type);

    @Query(value = "SELECT * FROM diary d " +
            "ORDER BY d.embedding <=> CAST(:queryVector AS vector) " +
            "LIMIT 3", nativeQuery = true)
    List<Diary> findTop3BySimilarity(@Param("queryVector") float[] queryVector);
}