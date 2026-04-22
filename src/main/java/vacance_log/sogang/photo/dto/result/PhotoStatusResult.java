package vacance_log.sogang.photo.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vacance_log.sogang.photo.dto.common.UserPhoto;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoStatusResult {
    private List<UserPhoto> userPhotos;
    private int totalCount;
    private int uploadedCount;
    private boolean isAllUploaded;

    public static PhotoStatusResult of(List<UserPhoto> userPhotos, int totalCount, int uploadedCount, boolean isAllUploaded) {
        return PhotoStatusResult.builder()
                .userPhotos(userPhotos)
                .totalCount(totalCount)
                .uploadedCount(uploadedCount)
                .isAllUploaded(isAllUploaded)
                .build();
    }
}
