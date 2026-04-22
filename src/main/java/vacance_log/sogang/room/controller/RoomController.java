package vacance_log.sogang.room.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vacance_log.sogang.global.dto.response.ApiResponse;
import vacance_log.sogang.room.service.RoomService;

@Tag(name = "Room", description = "여행 방 관리 및 종료 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    @Operation(
            summary = "여행 종료 및 다이어리 생성 (비동기)",
            description = "방 상태를 FINISHED로 변경하고, RabbitMQ를 통해 다이어리 생성을 비동기로 시작합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "여행 종료 및 다이어리 생성 프로세스 시작됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 종료된 방이거나 상태를 변경할 수 없음")
    })
    @PostMapping("/{roomId}/finish")
    public ResponseEntity<ApiResponse<String>> finishTravel(@PathVariable Long roomId) {
        roomService.initiateFinishProcess(roomId);
        log.info("🚀 [Travel Finish Initiated] RoomId: {} -> Processing started.", roomId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ApiResponse<>(true, 202, "여행 종료 프로세스가 시작되었습니다. 다이어리가 완성되면 알림이 발송됩니다.", "PROCESSING"));
    }
}
