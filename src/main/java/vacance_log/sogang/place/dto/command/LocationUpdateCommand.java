package vacance_log.sogang.place.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LocationUpdateCommand {
    private Long roomId;
    private Double lat;
    private Double lng;

    public static LocationUpdateCommand of(Long roomId, Double lat, Double lng) {
        return LocationUpdateCommand.builder()
                .roomId(roomId)
                .lat(lat)
                .lng(lng)
                .build();
    }
}
