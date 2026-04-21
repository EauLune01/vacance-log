package vacance_log.sogang.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vacance_log.sogang.place.domain.PhotoPlace;

import java.util.Optional;

public interface PhotoPlaceRepository extends JpaRepository<PhotoPlace,Long> {
    Optional<PhotoPlace> findByPlaceCode(String placeCode);
}
