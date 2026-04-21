package vacance_log.sogang.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vacance_log.sogang.diary.domain.Diary;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
}
