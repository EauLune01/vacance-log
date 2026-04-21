package vacance_log.sogang.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import vacance_log.sogang.notification.service.NotificationService;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationListener implements MessageListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(Message message, @Nullable byte[] pattern) {
        try {
            Map<String, Object> data = objectMapper.readValue(message.getBody(), Map.class);

            if (data != null) {

                Long userId = Long.valueOf(String.valueOf(data.get("userId")));
                String title = (String) data.get("title");
                String content = (String) data.get("content");

                notificationService.sendToClient(userId, title, content);
                log.info("🔔 [Final Notification Sent] User ID: {}, Title: {}", userId, title);
            }
        } catch (Exception e) {
            log.error("❌ Final failure in Redis notification processing: {}", e.getMessage());
        }
    }
}