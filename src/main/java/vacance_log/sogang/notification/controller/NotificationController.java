package vacance_log.sogang.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vacance_log.sogang.notification.service.NotificationService;

@Tag(name = "Notification", description = "SSE 기반 실시간 알림 API")
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * SSE 구독 시작
     */
    @Operation(
            summary = "SSE 구독",
            description = """
                    특정 userId에 대해 Server-Sent Events(SSE) 연결을 생성합니다.
                    
                    - 클라이언트는 해당 endpoint에 연결을 유지해야 합니다.
                    - Redis Pub/Sub 기반으로 실시간 추천 메시지를 수신합니다.
                    - 이벤트 타입: recommendation
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SSE 연결 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping(value = "/subscribe/{userId}", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter subscribe(
            @Parameter(description = "구독할 사용자 ID", example = "1")
            @PathVariable Long userId
    ) {
        log.info("🔌 Attempting SSE connection (userId: {})", userId);
        return notificationService.subscribe(userId);
    }
}