package vacance_log.sogang.diary.dto.response;

import lombok.Builder;
import lombok.Getter;
import vacance_log.sogang.diary.dto.result.DiaryDetailResult;

import java.util.List;

@Getter
@Builder
public class DiaryDetailResponse {
    private String title;
    private String content;
    private String cityName;
    private List<String> imageUrls;

    public static DiaryDetailResponse from(DiaryDetailResult result) {
        return DiaryDetailResponse.builder()
                .title(result.getTitle())
                .content(result.getContent())
                .cityName(result.getCityName())
                .imageUrls(result.getImageUrls())
                .build();
    }
}
