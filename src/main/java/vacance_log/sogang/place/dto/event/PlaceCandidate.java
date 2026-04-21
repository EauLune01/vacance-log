package vacance_log.sogang.place.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceCandidate {
    private String placeCode;
    private long distanceMeters;
}
