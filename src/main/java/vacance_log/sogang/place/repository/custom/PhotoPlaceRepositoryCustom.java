package vacance_log.sogang.place.repository.custom;

import java.util.Map;
import java.util.List;

public interface PhotoPlaceRepositoryCustom {
    Map<String, String> findPlaceNamesByCodes(List<String> placeCodes);
}
