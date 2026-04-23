package vacance_log.sogang.place.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vacance_log.sogang.place.dto.command.LocationUpdateCommand;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateRequest {
    @NotNull(message = "위도는 필수입니다.")
    private Double lat;

    @NotNull(message = "경도는 필수입니다.")
    private Double lng;

    public LocationUpdateCommand toCommand(Long roomId) {
        return LocationUpdateCommand.of(roomId, this.lat, this.lng);
    }
}
