package vacance_log.sogang.goods.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vacance_log.sogang.global.dto.response.ApiResponse;
import vacance_log.sogang.goods.dto.request.GoodsSearchRequest;
import vacance_log.sogang.goods.dto.response.GoodsSearchResponse;
import vacance_log.sogang.goods.dto.result.GoodsSearchResult;
import vacance_log.sogang.goods.service.GoodsSearchService;

@Tag(name = "Goods Search", description = "나만의 여행 굿즈(다이어리) 검색 및 대화 API")
@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsSearchController {

    private final GoodsSearchService goodsSearchService;

    @Operation(
            summary = "굿즈 기반 AI 대화",
            description = "유저 ID와 질문을 받아 AI 답변 및 관련 다이어리 상세 정보(이미지 포함)를 반환합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공적으로 답변과 굿즈 데이터를 생성했습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유저 또는 기록을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/search/{userId}")
    public ResponseEntity<ApiResponse<GoodsSearchResponse>> searchGoods(
            @Parameter(description = "유저 ID", example = "1")
            @PathVariable Long userId,
            @Valid @RequestBody GoodsSearchRequest request) {
        GoodsSearchResult result = goodsSearchService.searchAndChat(request.toCommand(userId));
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "성공적으로 굿즈 검색 및 답변 생성을 완료했습니다.", GoodsSearchResponse.from(result)));
    }
}
