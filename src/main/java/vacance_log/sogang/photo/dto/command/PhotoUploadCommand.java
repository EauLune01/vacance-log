package vacance_log.sogang.photo.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoUploadCommand {

    private Long roomId;
    private Long userId;
    private Long photoPlaceId;
    private MultipartFile file;

    public static PhotoUploadCommand of(Long roomId, Long userId, Long photoPlaceId, MultipartFile file) {
        return PhotoUploadCommand.builder()
                .roomId(roomId)
                .userId(userId)
                .photoPlaceId(photoPlaceId)
                .file(file)
                .build();
    }
}
