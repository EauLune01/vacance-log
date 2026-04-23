package vacance_log.sogang.diary.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import vacance_log.sogang.diary.domain.DiaryType;
import vacance_log.sogang.diary.dto.result.DiaryDetailResult;
import vacance_log.sogang.diary.dto.result.GroupDiaryResult;
import vacance_log.sogang.diary.dto.result.PersonalDiaryResult;
import vacance_log.sogang.photo.dto.result.PhotoInfo;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiaryResponse {
    private String title;
    private String cityName;
    private DiaryType type; // INDIVIDUAL or GROUP

    // Group 전용 필드
    private String content;
    private List<String> imageUrls;

    // Personal 전용 필드
    private List<PhotoInfo> photos;

    public static DiaryResponse from(DiaryDetailResult result) {
        var builder = DiaryResponse.builder()
                .title(result.getTitle())
                .cityName(result.getCityName())
                .type(result.getType());

        if (result instanceof GroupDiaryResult group) {
            return builder
                    .content(group.getContent())
                    .imageUrls(group.getImageUrls())
                    .build();
        } else if (result instanceof PersonalDiaryResult personal) {
            return builder
                    .photos(personal.getPhotos())
                    .build();
        }
        return builder.build();
    }
}
