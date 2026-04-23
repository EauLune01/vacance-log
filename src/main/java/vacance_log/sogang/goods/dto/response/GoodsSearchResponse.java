package vacance_log.sogang.goods.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vacance_log.sogang.diary.dto.response.DiaryResponse;
import vacance_log.sogang.goods.dto.result.GoodsSearchResult;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsSearchResponse {
    private String answer;
    private List<DiaryResponse> diaries;

    public static GoodsSearchResponse from(GoodsSearchResult result) {
        return GoodsSearchResponse.builder()
                .answer(result.getAnswer())
                .diaries(result.getDiaries().stream()
                        .map(DiaryResponse::from)
                        .toList())
                .build();
    }
}
