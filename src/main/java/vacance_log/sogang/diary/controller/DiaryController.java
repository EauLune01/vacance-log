package vacance_log.sogang.diary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vacance_log.sogang.diary.domain.DiaryType;
import vacance_log.sogang.diary.dto.command.DiaryQueryCommand;
import vacance_log.sogang.diary.dto.response.DiaryDetailResponse;
import vacance_log.sogang.diary.service.DiaryService;
import vacance_log.sogang.global.dto.response.ApiResponse;

@Tag(name = "Diary", description = "다이어리 상세 조회 API")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @Operation(
            summary = "개인 다이어리 상세 조회",
            description = "방 ID와 유저 ID를 경로로 받아 해당 유저의 개인 일기를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "개인 다이어리 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 유저의 일기를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{roomId}/diaries/INDIVIDUAL/{userId}")
    public ResponseEntity<ApiResponse<DiaryDetailResponse>> getIndividualDiary(
            @Parameter(description = "방 ID", example = "1") @PathVariable Long roomId,
            @Parameter(description = "유저 ID", example = "10") @PathVariable Long userId
    ) {
        DiaryQueryCommand command = DiaryQueryCommand.of(roomId, userId, DiaryType.INDIVIDUAL);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "개인 다이어리를 성공적으로 조회하였습니다.", DiaryDetailResponse.from(diaryService.getDiaryDetail(command))));
    }

    @Operation(
            summary = "그룹 다이어리 상세 조회",
            description = "방 ID를 경로로 받아 해당 방 멤버 전체의 그룹 다이어리를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "그룹 다이어리 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 방의 그룹 일기를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{roomId}/diaries/GROUP")
    public ResponseEntity<ApiResponse<DiaryDetailResponse>> getGroupDiary(
            @Parameter(description = "방 ID", example = "1") @PathVariable Long roomId
    ) {
        DiaryQueryCommand command = DiaryQueryCommand.of(roomId, null, DiaryType.GROUP);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "그룹 다이어리를 성공적으로 조회하였습니다.", DiaryDetailResponse.from(diaryService.getDiaryDetail(command))));
    }
}