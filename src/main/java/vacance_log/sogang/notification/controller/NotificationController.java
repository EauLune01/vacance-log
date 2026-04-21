package vacance_log.sogang.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vacance_log.sogang.notification.service.NotificationService;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * SSE 구독 시작
     */
    @GetMapping(value = "/subscribe/{userId}", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter subscribe(@PathVariable Long userId) {
        log.info("🔌 Attempting SSE connection (userId: {})", userId);
        return notificationService.subscribe(userId);
    }
}