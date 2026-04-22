package vacance_log.sogang.photo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vacance_log.sogang.photo.dto.common.UserPhoto;
import vacance_log.sogang.photo.dto.result.PhotoStatusResult;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoStatusResponse {
    private List<UserPhoto> userPhotos;
    private int totalCount;
    private int uploadedCount;
    private boolean isAllUploaded;

    public static PhotoStatusResponse from(PhotoStatusResult result) {
        return PhotoStatusResponse.builder()
                .userPhotos(result.getUserPhotos())
                .totalCount(result.getTotalCount())
                .uploadedCount(result.getUploadedCount())
                .isAllUploaded(result.isAllUploaded())
                .build();
    }
}
