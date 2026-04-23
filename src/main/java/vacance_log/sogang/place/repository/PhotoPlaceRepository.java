package vacance_log.sogang.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vacance_log.sogang.place.domain.PhotoPlace;
import vacance_log.sogang.place.repository.custom.PhotoPlaceRepositoryCustom;

import java.util.Optional;

public interface PhotoPlaceRepository extends JpaRepository<PhotoPlace,Long>, PhotoPlaceRepositoryCustom {
    Optional<PhotoPlace> findByPlaceCode(String placeCode);
}
