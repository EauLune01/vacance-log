package vacance_log.sogang.photo.dto.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class PhotoAnalysisEvent {

    private Long photoId;
    private String s3Url;

    public static PhotoAnalysisEvent of(Long photoId, String s3Url) {
        return new PhotoAnalysisEvent(photoId, s3Url);
    }
}
