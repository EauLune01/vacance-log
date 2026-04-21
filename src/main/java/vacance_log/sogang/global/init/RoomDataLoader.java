package vacance_log.sogang.global.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
@Order(4)
public class RoomDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (roomRepository.count() > 0) return;

        log.info("Starting Marrakech travel room creation and member invitation...");

        User host = userRepository.findByNickname("yihyun_1208")
                .orElseThrow(() -> new RuntimeException("Host not found."));
        User guest1 = userRepository.findByNickname("yena.jigumina").get();
        User guest2 = userRepository.findByNickname("triplescosmos").get();
        User guest3 = userRepository.findByNickname("do_0000").get();

        City marrakech = cityRepository.findByName("Marrakech")
                .orElseThrow(() -> new RuntimeException("City not found."));

        Room room = Room.createRoom("Marrakech Trip", marrakech);
        roomRepository.save(room);

        userRoomRepository.save(UserRoom.createUserRoom(host, room, ParticipantRole.HOST));
        userRoomRepository.save(UserRoom.createUserRoom(guest1, room, ParticipantRole.GUEST));
        userRoomRepository.save(UserRoom.createUserRoom(guest2, room, ParticipantRole.GUEST));
        userRoomRepository.save(UserRoom.createUserRoom(guest3, room, ParticipantRole.GUEST));

        log.info("Successfully created 'Marrakech Trip' room! (Host: Yihyun Jo, Members: Yena Choi, Seoyeon Yoon, Doyoung Kim)");
    }
}