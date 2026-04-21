package vacance_log.sogang.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vacance_log.sogang.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByNickname(String nickname);
}
