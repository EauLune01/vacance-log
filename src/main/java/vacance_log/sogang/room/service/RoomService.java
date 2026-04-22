package vacance_log.sogang.room.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vacance_log.sogang.global.config.rabbitMq.RabbitMqConfig;
import vacance_log.sogang.global.exception.room.RoomNotFoundException;
import vacance_log.sogang.global.exception.room.TravelAlreadyFinishedException;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.room.domain.RoomStatus;
import vacance_log.sogang.room.repository.RoomRepository;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {
    private final RoomRepository roomRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void initiateFinishProcess(Long roomId) {
        Room room = getValidRoom(roomId);

        if (room.getStatus() == RoomStatus.FINISHED) {
            throw new TravelAlreadyFinishedException("이미 종료된 여행입니다.");
        }

        // 1. 상태 변경
        room.finishTravel();

        // 2. 🚀 RabbitMQ로 "여행 종료 알림" 전송
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.TRAVEL_EXCHANGE,
                RabbitMqConfig.DIARY_GENERATE_ROUTING,
                roomId
        );
    }

    private Room getValidRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found."));

        if (room.getStatus() == RoomStatus.FINISHED) {
            throw new TravelAlreadyFinishedException("이미 종료된 여행입니다.");
        }

        return room;
    }
}
