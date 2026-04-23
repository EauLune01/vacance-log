package vacance_log.sogang.photo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vacance_log.sogang.global.dto.response.ApiResponse;
import vacance_log.sogang.photo.dto.command.PhotoUploadCommand;
import vacance_log.sogang.photo.dto.request.PhotoUploadRequest;
import vacance_log.sogang.photo.dto.response.PhotoStatusResponse;
import vacance_log.sogang.photo.dto.result.PhotoStatusResult;
import vacance_log.sogang.photo.service.PhotoQueryService;
import vacance_log.sogang.photo.service.PhotoService;

@Tag(name="photo", description = "사진 업로드 및 조회 API")
@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@Slf4j
public class PhotoController {

    private final PhotoService photoService;
    private final PhotoQueryService photoQueryService;

    @Operation(
            summary = "사진 업로드 및 AI 메모 생성 요청",
            description = "사진을 S3에 업로드하고 GPT-4o를 통해 20자 이내의 영문 메모를 비동기로 생성합니다. 'request'는 JSON 형식의 문자열로 보내주세요."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "사진 업로드 및 분석 요청 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 검증 실패 (필수 ID 누락 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "방, 유저 또는 장소 정보를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "S3 업로드 실패 또는 서버 오류")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> uploadPhoto(
            @Parameter(
                    name = "request",
                    description = "사진 업로드 상세 정보 (JSON)",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PhotoUploadRequest.class)
                    )
            )
            @RequestPart(value = "request") @Valid PhotoUploadRequest request,

            @Parameter(
                    description = "업로드할 이미지 파일 (1장)",
                    required = true
            )
            @RequestPart(value = "file") MultipartFile file
    ) {
        PhotoUploadCommand command = PhotoUploadRequest.of(request, file);
        photoService.createPhoto(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, 201, "사진 업로드 성공 및 AI 메모 생성 요청 완료", null));
    }

    @Operation(
            summary = "장소별 사진 업로드 현황 조회",
            description = """
                특정 방(roomId) 내의 특정 장소(photoPlaceId)에 대해 
                멤버들의 사진 업로드 여부와 S3 URL을 계층형 경로로 조회합니다.
                """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "현황 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "방 또는 장소를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{roomId}/places/{photoPlaceId}/status")
    public ResponseEntity<ApiResponse<PhotoStatusResponse>> getPhotoStatus(
            @Parameter(description = "조회할 방 ID", example = "1")
            @PathVariable Long roomId,
            @Parameter(description = "조회할 장소 ID", example = "10")
            @PathVariable Long photoPlaceId
    ) {
        PhotoStatusResult result = photoQueryService.getPhotoStatus(roomId, photoPlaceId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "현황 조회를 성공하였습니다.", PhotoStatusResponse.from(result)));
    }
}
