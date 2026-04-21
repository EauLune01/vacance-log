package vacance_log.sogang.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final String CHANNEL_PREFIX = "notif:user:";

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(60L * 1000 * 60);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        sendToClient(userId, "System", "Connected to Redis Pub/Sub");

        return emitter;
    }

    public void send(Long userId, String title, String content, String placeCode) {
        Map<String, String> data = Map.of(
                "userId", String.valueOf(userId),
                "title", title,
                "content", content,
                "placeCode", placeCode
        );
        redisTemplate.convertAndSend(CHANNEL_PREFIX + userId, data);
    }

    public void sendToClient(Long userId, String title, String content) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("recommendation")
                        .data(objectMapper.writeValueAsString(
                                Map.of("title", title, "content", content)
                        )));
            } catch (IOException e) {
                emitters.remove(userId);
                log.error("Failed to send SSE for user: {}", userId);
            }
        }
    }
}