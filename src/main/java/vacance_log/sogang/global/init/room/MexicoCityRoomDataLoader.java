package vacance_log.sogang.global.init.room;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vacance_log.sogang.global.exception.user.UserNotFoundException;
import vacance_log.sogang.place.domain.City;
import vacance_log.sogang.place.repository.CityRepository;
import vacance_log.sogang.room.domain.ParticipantRole;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.room.domain.UserRoom;
import vacance_log.sogang.room.repository.RoomRepository;
import vacance_log.sogang.room.repository.UserRoomRepository;
import vacance_log.sogang.user.domain.User;
import vacance_log.sogang.user.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(9)
public class MexicoCityRoomDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (roomRepository.findByTitle("Viva Mexico City Trip").isPresent()) return;

        User host = userRepository.findByNickname("triplescosmos")
                .orElseThrow(() -> new UserNotFoundException("Host (Seoyeon) not found."));
        User guest1 = userRepository.findByNickname("yihyun_1208").get();
        User guest2 = userRepository.findByNickname("yena.jigumina").get();
        User guest3 = userRepository.findByNickname("do_0000").get();

        City mexicoCity = cityRepository.findByName("Mexico City")
                .orElseThrow(() -> new RuntimeException("City not found."));

        Room room = Room.createRoom("Viva Mexico City Trip", mexicoCity);
        roomRepository.save(room);

        userRoomRepository.save(UserRoom.createUserRoom(host, room, ParticipantRole.HOST));
        userRoomRepository.save(UserRoom.createUserRoom(guest1, room, ParticipantRole.GUEST));
        userRoomRepository.save(UserRoom.createUserRoom(guest2, room, ParticipantRole.GUEST));
        userRoomRepository.save(UserRoom.createUserRoom(guest3, room, ParticipantRole.GUEST));

        log.info("Successfully created 'Viva Mexico City' room! (Host: Seoyeon)");
    }
}
