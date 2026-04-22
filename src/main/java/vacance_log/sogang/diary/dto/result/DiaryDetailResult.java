package vacance_log.sogang.diary.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class DiaryDetailResult {
    private final String title;
    private final String content;
    private final String cityName;
    private final List<String> imageUrls;

    public static DiaryDetailResult of(String title, String content, String cityName, List<String> imageUrls) {
        return new DiaryDetailResult(title, content, cityName, imageUrls);
    }
}