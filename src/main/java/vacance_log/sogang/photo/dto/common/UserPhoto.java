package vacance_log.sogang.photo.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPhoto {
    private Long userId;
    private String nickname;
    private String s3Url;
    private boolean isUploaded;

    public static UserPhoto of(Long userId, String nickname, String s3Url, boolean isUploaded) {
        return UserPhoto.builder()
                .userId(userId)
                .nickname(nickname)
                .s3Url(s3Url)
                .isUploaded(isUploaded)
                .build();
    }
}
