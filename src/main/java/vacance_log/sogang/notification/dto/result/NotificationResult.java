package vacance_log.sogang.notification.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResult {

    private Long userId;
    private String title;
    private String content;
    private String type;

    private Long roomId;
    private String placeCode;
    private Long photoPlaceId;

    public static NotificationResult ofPlaceRecommendation(
            Long userId,
            Long roomId,
            String title,
            String content,
            String placeCode,
            Long photoPlaceId
    ) {
        return NotificationResult.builder()
                .userId(userId)
                .roomId(roomId)
                .title(title)
                .content(content)
                .type("PLACE_RECOMMENDATION")
                .placeCode(placeCode)
                .photoPlaceId(photoPlaceId)
                .build();
    }

    public static NotificationResult ofDiaryComplete(
            Long userId,
            String title,
            String content
    ) {
        return NotificationResult.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .type("DIARY_COMPLETE")
                .build();
    }

    public static NotificationResult ofConnected(Long userId) {
        return NotificationResult.builder()
                .userId(userId)
                .title("System")
                .content("Connected to Redis Pub/Sub")
                .type("CONNECTED")
                .build();
    }
}
