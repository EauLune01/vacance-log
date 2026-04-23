package vacance_log.sogang.diary.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import vacance_log.sogang.diary.domain.DiaryType;
import vacance_log.sogang.photo.dto.result.PhotoInfo;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PersonalDiaryResult implements DiaryDetailResult {
    private String title;
    private String cityName;
    private List<PhotoInfo> photos;

    @Override
    public DiaryType getType() { return DiaryType.INDIVIDUAL; }

    public static PersonalDiaryResult of(String title, String cityName, List<PhotoInfo> photos) {
        return PersonalDiaryResult.builder()
                .title(title)
                .cityName(cityName)
                .photos(photos)
                .build();
    }
}
