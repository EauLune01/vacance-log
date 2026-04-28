package vacance_log.sogang.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vacance_log.sogang.notification.dto.response.NotificationResponse;
import vacance_log.sogang.notification.dto.result.NotificationResult;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    private static final String CHANNEL_PREFIX = "notif:user:";
    private static final String HISTORY_PREFIX = "notif:history:";
    private static final long TIMEOUT = 60L * 60 * 1000;

    private static final String DIARY_TITLE = "여행 일기 완성! ✍️";
    private static final String DIARY_CONTENT_FORMAT =
            "[%s] 여행 일기가 완성됐어요. 지금 확인해보세요!";

    // =========================
    // Subscribe
    // =========================
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        sendToClient(NotificationResponse.from(NotificationResult.ofConnected(userId)));

        sendMissedNotifications(userId);

        return emitter;
    }

    // =========================
    // Send (Recommendation)
    // =========================
    public void sendPlaceRecommendation(
            Long userId,
            Long roomId,
            String title,
            String content,
            String placeCode,
            Long photoPlaceId
    ) {

        NotificationResponse response = NotificationResponse.from(
                NotificationResult.ofPlaceRecommendation(
                        userId, roomId, title, content, placeCode, photoPlaceId
                )
        );

        redisTemplate.convertAndSend(CHANNEL_PREFIX + userId, response);

        saveHistory(userId, response);
    }

    // =========================
    // Send (Diary)
    // =========================
    public void sendDiaryNotification(Long userId, String roomTitle) {

        String content = String.format(DIARY_CONTENT_FORMAT, roomTitle);

        NotificationResponse response = NotificationResponse.from(
                NotificationResult.ofDiaryComplete(userId, DIARY_TITLE, content)
        );

        redisTemplate.convertAndSend(CHANNEL_PREFIX + userId, response);

        saveHistory(userId, response);
    }

    // =========================
    // History
    // =========================
    private void saveHistory(Long userId, NotificationResponse response) {
        String key = HISTORY_PREFIX + userId;

        redisTemplate.opsForList().rightPush(key, response);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }

    private void sendMissedNotifications(Long userId) {
        String key = HISTORY_PREFIX + userId;

        List<Object> list = redisTemplate.opsForList().range(key, 0, -1);

        if (list != null && !list.isEmpty()) {
            log.info("📩 [Recovery] {} messages for user={}", list.size(), userId);

            list.forEach(obj -> {
                NotificationResponse res =
                        objectMapper.convertValue(obj, NotificationResponse.class);
                sendToClient(res);
            });

            redisTemplate.delete(key);
        }
    }

    // =========================
    // SSE send
    // =========================
    public void sendToClient(NotificationResponse response) {
        SseEmitter emitter = emitters.get(response.getUserId());

        if (emitter == null) {
            log.warn("⚠️ emitter 없음 userId={}", response.getUserId());
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name(response.getType())
                    .data(response));

        } catch (IOException e) {
            emitters.remove(response.getUserId());
            log.error("❌ SSE 전송 실패 userId={}", response.getUserId(), e);
        }
    }
}