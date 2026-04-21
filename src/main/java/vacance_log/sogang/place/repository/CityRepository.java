package vacance_log.sogang.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vacance_log.sogang.place.domain.City;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City,Long> {
    Optional<City> findByName(String name);
}
