package vacance_log.sogang.place.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vacance_log.sogang.global.dto.response.ApiResponse;
import vacance_log.sogang.place.service.LocationService;

@Tag(name = "Location", description = "실시간 위치 기반 장소 감지 및 추천 API")
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @Operation(
            summary = "유저 실시간 위치 업데이트",
            description = "방장(혹은 유저)의 현재 위경도를 받아 주변 200m 이내의 추천 후보지를 감지하고 RabbitMQ 이벤트를 발행합니다."
    )
    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<String>> updateLocation(
            @Parameter(description = "여행 방 ID", example = "1")
            @PathVariable Long roomId,
            @Parameter(description = "현재 위도 (Latitude)", example = "31.6258")
            @RequestParam Double lat,
            @Parameter(description = "현재 경도 (Longitude)", example = "-7.9891")
            @RequestParam Double lng) {
        locationService.processLocationUpdate(roomId, lat, lng);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "위치 정보 업데이트 및 장소 감지 로직이 성공적으로 실행되었습니다.", "Location process triggered for Room ID: " + roomId));
    }
}
