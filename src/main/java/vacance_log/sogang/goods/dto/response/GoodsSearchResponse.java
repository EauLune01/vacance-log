package vacance_log.sogang.goods.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import vacance_log.sogang.diary.dto.response.DiaryDetailResponse;
import vacance_log.sogang.goods.dto.result.GoodsSearchResult;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class GoodsSearchResponse {
    private final String answer;

    private final List<DiaryDetailResponse> diaries;

    public static GoodsSearchResponse from(GoodsSearchResult result) {
        return GoodsSearchResponse.builder()
                .answer(result.getAnswer())
                .diaries(result.getDiaries().stream()
                        .map(DiaryDetailResponse::from)
                        .toList())
                .build();
    }
}
