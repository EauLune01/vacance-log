package vacance_log.sogang.place.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocationEvent {
    private Long roomId;
    private List<PlaceCandidate> candidates;
    private Double lat;
    private Double lng;

    public static LocationEvent of(Long roomId, List<PlaceCandidate> candidates, Double lat, Double lng) {
        LocationEvent event = new LocationEvent();
        event.roomId = roomId;
        event.candidates = candidates;
        event.lat = lat;
        event.lng = lng;
        return event;
    }
}