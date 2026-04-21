package vacance_log.sogang.global.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import vacance_log.sogang.user.domain.Gender;
import vacance_log.sogang.user.domain.Persona;
import vacance_log.sogang.user.domain.User;
import vacance_log.sogang.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class UserDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("User data already exists. Skipping data loading.");
            return;
        }

        log.info("Starting user data creation...");


        User ihyun = User.createUser(
                "Yihyun Jo", "yihyun_1208",
                LocalDate.of(1999, 12, 8),
                Gender.FEMALE, Persona.PHOTO_TRAVELER
        );


        User yena = User.createUser(
                "Yena Choi", "yena.jigumina",
                LocalDate.of(1999, 9, 29),
                Gender.FEMALE, Persona.FOODIE
        );

        User seoyeon = User.createUser(
                "Seoyoun Yoon", "triplescosmos",
                LocalDate.of(2003, 8, 6),
                Gender.FEMALE, Persona.RELAXER
        );


        User doyoung = User.createUser(
                "Doyoung Kim", "do_0000",
                LocalDate.of(2003, 10, 2),
                Gender.MALE, Persona.EXPLORER
        );

        userRepository.saveAll(List.of(ihyun, yena, seoyeon, doyoung));

        log.info("✅ User data loading completed: {}, {}, {}, {}",
                ihyun.getName(), yena.getName(), seoyeon.getName(), doyoung.getName());
    }
}