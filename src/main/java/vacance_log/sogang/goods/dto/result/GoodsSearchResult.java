package vacance_log.sogang.goods.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import vacance_log.sogang.diary.dto.result.DiaryDetailResult;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GoodsSearchResult {
    private final String answer;
    private final List<DiaryDetailResult> diaries;
    private final boolean hasContext;

    public static GoodsSearchResult of(String answer, List<DiaryDetailResult> diaries, boolean hasContext) {
        return new GoodsSearchResult(answer, diaries, hasContext);
    }
}
