package vacance_log.sogang.notification.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final String CHANNEL_PREFIX = "notif:user:";
    private static final String HISTORY_PREFIX = "notif:history:";
    private static final String DIARY_TITLE = "Travel Diaries Ready! ✍️";
    private static final String DIARY_CONTENT_FORMAT = "The diaries for [%s] are now complete. Go check them out!";

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(60L * 1000 * 60);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));

        // 1. 연결 확인용 첫 메시지
        sendToClient(userId, "System", "Connected to Redis Pub/Sub");

        // 2. 🏛️ 오프라인일 때 쌓인 미수신 알림 전송 (Redis List 활용)
        sendMissedNotifications(userId);

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

    public void sendDiaryNotification(Long userId, String roomTitle) {
        String content = String.format(DIARY_CONTENT_FORMAT, roomTitle);
        Map<String, String> data = Map.of(
                "userId", String.valueOf(userId),
                "title", DIARY_TITLE,
                "content", content,
                "type", "DIARY_COMPLETE"
        );

        // 1. 실시간 전송 (Pub/Sub)
        redisTemplate.convertAndSend(CHANNEL_PREFIX + userId, data);

        // 2. 🏛️ 이력 저장 (TTL 24시간)
        String historyKey = HISTORY_PREFIX + userId;
        redisTemplate.opsForList().rightPush(historyKey, data);
        redisTemplate.expire(historyKey, 24, TimeUnit.HOURS);
    }


    private void sendMissedNotifications(Long userId) {
        String historyKey = HISTORY_PREFIX + userId;

        List<Object> missedNotifs = redisTemplate.opsForList().range(historyKey, 0, -1);

        if (missedNotifs != null && !missedNotifs.isEmpty()) {
            log.info("📩 [Recovery] Sending {} missed notifications to User: {}", missedNotifs.size(), userId);

            for (Object notif : missedNotifs) {
                Map<String, String> data = objectMapper.convertValue(notif, new TypeReference<>() {});
                sendToClient(userId, data.get("title"), data.get("content"));
            }

            redisTemplate.delete(historyKey);
        }
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