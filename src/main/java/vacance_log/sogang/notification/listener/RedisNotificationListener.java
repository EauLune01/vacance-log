package vacance_log.sogang.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import vacance_log.sogang.notification.dto.response.NotificationResponse;
import vacance_log.sogang.notification.service.NotificationService;

import java.nio.charset.StandardCharsets;


@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationListener implements MessageListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {
        try {
            byte[] body = message.getBody();

            String raw = new String(body, StandardCharsets.UTF_8);

            log.info("📩 [Redis Raw] {}", raw);

            NotificationResponse response =
                    objectMapper.readValue(raw, NotificationResponse.class);

            notificationService.sendToClient(response);

            log.info("🔔 [SSE Sent] userId={}, type={}",
                    response.getUserId(),
                    response.getType()
            );

        } catch (Exception e) {
            log.error("❌ Redis → SSE 실패", e);
        }
    }
}