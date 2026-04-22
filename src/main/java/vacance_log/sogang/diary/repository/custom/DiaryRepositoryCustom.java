package vacance_log.sogang.diary.repository.custom;

import vacance_log.sogang.diary.domain.DiaryType;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.user.domain.User;

import java.util.Optional;

public interface DiaryRepositoryCustom {
    Optional<String> findContent(Room room, DiaryType type, User user);
}
