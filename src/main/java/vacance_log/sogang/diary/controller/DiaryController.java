package vacance_log.sogang.diary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vacance_log.sogang.diary.dto.command.DiaryQueryCommand;
import vacance_log.sogang.diary.dto.request.DiaryQueryRequest;
import vacance_log.sogang.diary.dto.response.DiaryDetailResponse;
import vacance_log.sogang.diary.dto.result.DiaryDetailResult;
import vacance_log.sogang.diary.service.DiaryService;

@Tag(name = "Diary", description = "에세이 조회 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/diaries")
public class DiaryController {

    private final DiaryService diaryService;

    @Operation(
            summary = "다이어리 상세 조회",
            description = "방 ID와 타입을 통해 개인/그룹 다이어리를 조회합니다. INDIVIDUAL 타입일 경우 userId는 필수입니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "다이어리 조회 성공",
                    content = @Content(schema = @Schema(implementation = DiaryDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 다이어리를 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터 요청")
    })
    @GetMapping
    public ResponseEntity<vacance_log.sogang.global.dto.response.ApiResponse<DiaryDetailResponse>> getDiary(
            @ParameterObject @Valid @ModelAttribute DiaryQueryRequest request
    ) {
        log.info("📖 Diary Request Trace: Room={}, Type={}, User={}",
                request.getRoomId(), request.getType(), request.getUserId());
        DiaryQueryCommand command = DiaryQueryRequest.toCommand(request);
        DiaryDetailResult result = diaryService.getDiaryDetail(command);
        return ResponseEntity.ok(new vacance_log.sogang.global.dto.response.ApiResponse<>(true, 200, "다이어리를 성공적으로 조회하였습니다.", DiaryDetailResponse.from(result)));
    }
}