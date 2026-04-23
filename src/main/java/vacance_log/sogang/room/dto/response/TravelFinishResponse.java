package vacance_log.sogang.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TravelFinishResponse {
    private Long roomId;

    private String status;

    private LocalDateTime initiatedAt;

    public static TravelFinishResponse of(Long roomId, String status) {
        return TravelFinishResponse.builder()
                .roomId(roomId)
                .status(status)
                .initiatedAt(LocalDateTime.now())
                .build();
    }
}
