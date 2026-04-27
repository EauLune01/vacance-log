package vacance_log.sogang.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vacance_log.sogang.notification.dto.result.NotificationResult;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long userId;
    private String title;
    private String content;
    private String type;

    private Long roomId;
    private String placeCode;
    private Long photoPlaceId;

    public static NotificationResponse from(NotificationResult result) {
        return NotificationResponse.builder()
                .userId(result.getUserId())
                .title(result.getTitle())
                .content(result.getContent())
                .type(result.getType())
                .roomId(result.getRoomId())
                .placeCode(result.getPlaceCode())
                .photoPlaceId(result.getPhotoPlaceId())
                .build();
    }
}