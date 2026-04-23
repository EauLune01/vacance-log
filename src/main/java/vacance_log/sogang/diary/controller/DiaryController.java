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
import vacance_log.sogang.diary.dto.response.GroupDiaryResponse;
import vacance_log.sogang.diary.dto.response.PersonalDiaryResponse;
import vacance_log.sogang.diary.dto.result.DiaryDetailResult;
import vacance_log.sogang.diary.dto.result.GroupDiaryResult;
import vacance_log.sogang.diary.dto.result.PersonalDiaryResult;
import vacance_log.sogang.diary.service.DiaryQueryService;
import vacance_log.sogang.global.dto.response.ApiResponse;

@Tag(name = "Diary", description = "다이어리 상세 조회 API")
@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryQueryService diaryQueryService;

    @Operation(
            summary = "개인 다이어리 상세 조회",
            description = "방 ID와 유저 ID를 통해 해당 유저가 올린 사진과 AI 메모 리스트(굿즈)를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "개인 다이어리 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 유저의 여행 기록을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{roomId}/personal/{userId}")
    public ResponseEntity<ApiResponse<PersonalDiaryResponse>> getIndividualDiary(
            @Parameter(description = "방 ID", example = "1") @PathVariable Long roomId,
            @Parameter(description = "유저 ID", example = "1") @PathVariable Long userId
    ) {
        DiaryQueryCommand command = DiaryQueryCommand.of(roomId, userId, DiaryType.INDIVIDUAL);
        DiaryDetailResult result = diaryQueryService.getPersonalDiary(command);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "개인 다이어리를 성공적으로 조회하였습니다.", PersonalDiaryResponse.from((PersonalDiaryResult) result)));
    }

    @Operation(summary = "그룹 다이어리 상세 조회", description = "방 멤버 전체의 통합 에세이와 전체 사진 리스트를 조회합니다.")
    @GetMapping("/{roomId}/group")
    public ResponseEntity<ApiResponse<GroupDiaryResponse>> getGroupDiary(
            @Parameter(description = "방 ID", example = "1") @PathVariable Long roomId
    ) {
        DiaryDetailResult result = diaryQueryService.getGroupDiary(roomId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "그룹 다이어리를 성공적으로 조회하였습니다.", GroupDiaryResponse.from((GroupDiaryResult) result)));
    }
}