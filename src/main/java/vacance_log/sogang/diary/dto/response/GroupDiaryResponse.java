package vacance_log.sogang.diary.dto.response;

import lombok.Builder;
import lombok.Getter;
import vacance_log.sogang.diary.dto.result.GroupDiaryResult;

import java.util.List;

@Getter
@Builder
public class GroupDiaryResponse {
    private String title;
    private String content;
    private String cityName;
    private List<String> imageUrls;

    public static GroupDiaryResponse from(GroupDiaryResult result) {
        return GroupDiaryResponse.builder()
                .title(result.getTitle())
                .content(result.getContent())
                .cityName(result.getCityName())
                .imageUrls(result.getImageUrls())
                .build();
    }
}
