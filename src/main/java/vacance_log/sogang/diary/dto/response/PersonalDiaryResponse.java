package vacance_log.sogang.diary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import vacance_log.sogang.diary.dto.result.PersonalDiaryResult;
import vacance_log.sogang.photo.dto.result.PhotoInfo;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PersonalDiaryResponse {
    private String title;
    private String cityName;
    private List<PhotoInfo> photos;

    public static PersonalDiaryResponse from(PersonalDiaryResult result) {
        return PersonalDiaryResponse.builder()
                .title(result.getTitle())
                .cityName(result.getCityName())
                .photos(result.getPhotos())
                .build();
    }
}
