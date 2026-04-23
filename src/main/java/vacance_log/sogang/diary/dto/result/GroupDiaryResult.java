package vacance_log.sogang.diary.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import vacance_log.sogang.diary.domain.DiaryType;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GroupDiaryResult implements DiaryDetailResult {
    private String title;
    private String content;
    private String cityName;
    private List<String> imageUrls;

    @Override
    public DiaryType getType() { return DiaryType.GROUP; }

    public static GroupDiaryResult of(String title, String content, String cityName, List<String> imageUrls) {
        return GroupDiaryResult.builder()
                .title(title)
                .content(content)
                .cityName(cityName)
                .imageUrls(imageUrls)
                .build();
    }
}