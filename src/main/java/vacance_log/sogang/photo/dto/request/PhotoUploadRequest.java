package vacance_log.sogang.photo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import vacance_log.sogang.photo.dto.command.PhotoUploadCommand;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoUploadRequest {

    @NotNull(message = "여행 방 ID는 필수입니다.")
    private Long roomId;

    @NotNull(message = "유저 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "추천 장소 ID는 필수입니다. 알림을 통해 방문한 장소를 선택해주세요.")
    private Long photoPlaceId;

    public static PhotoUploadCommand of(PhotoUploadRequest request, MultipartFile file) {
        return PhotoUploadCommand.builder()
                .roomId(request.getRoomId())
                .userId(request.getUserId())
                .photoPlaceId(request.getPhotoPlaceId())
                .file(file)
                .build();
    }
}
