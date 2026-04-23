package vacance_log.sogang.place.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vacance_log.sogang.global.dto.response.ApiResponse;
import vacance_log.sogang.place.dto.request.LocationUpdateRequest;
import vacance_log.sogang.place.service.LocationService;

@Tag(name = "Location", description = "실시간 위치 기반 장소 감지 및 추천 API")
@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @Operation(
            summary = "유저 실시간 위치 업데이트",
            description = "방 ID는 경로로, 위경도는 JSON 바디로 받아 주변 200m 이내의 장소를 감지하고 이벤트를 발행합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "위치 정보 업데이트 및 장소 감지 로직 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 위경도 값"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 여행 방 ID")
    })
    @PutMapping("/{roomId}")
    public ResponseEntity<ApiResponse<String>> updateLocation(
            @Parameter(description = "여행 방 ID", example = "1")
            @PathVariable Long roomId,
            @Valid @RequestBody LocationUpdateRequest request) {

        locationService.processLocationUpdate(request.toCommand(roomId));

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                200,
                "위치 정보 업데이트 및 장소 감지 로직이 성공적으로 실행되었습니다.",
                "Location process triggered for Room ID: " + roomId
        ));
    }
}
