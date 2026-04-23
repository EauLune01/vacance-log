package vacance_log.sogang.photo.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vacance_log.sogang.photo.domain.Photo;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoInfo {
    private Long photoId;
    private String s3Url;
    private String description;


    public static PhotoInfo from(Photo photo) {
        return PhotoInfo.builder()
                .photoId(photo.getId())
                .s3Url(photo.getS3Url())
                .description(photo.getDescription())
                .build();
    }
}
