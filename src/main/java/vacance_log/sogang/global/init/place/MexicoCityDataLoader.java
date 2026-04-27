package vacance_log.sogang.global.init.place;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vacance_log.sogang.place.domain.City;
import vacance_log.sogang.place.domain.PhotoPlace;
import vacance_log.sogang.place.repository.CityRepository;
import vacance_log.sogang.place.repository.PhotoPlaceRepository;

@Slf4j
@Component
@Order(5)
@RequiredArgsConstructor
public class MexicoCityDataLoader implements CommandLineRunner {

    private final CityRepository cityRepository;
    private final PhotoPlaceRepository photoPlaceRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String GEO_KEY_PREFIX = "city:places:";

    @Override
    public void run(String... args) throws Exception {
        if (cityRepository.findByName("Mexico City").isPresent()) {
            log.info("⏩ Mexico City data already exists. Skipping.");
            return;
        }

        City mexicoCity = City.createCity("Mexico City", "Mexico", 19.4326, -99.1332);
        cityRepository.saveAndFlush(mexicoCity);
        log.info("🌆 City created: {}", mexicoCity.getName());

        loadMexicoCityPlaces(mexicoCity);

        log.info("✅ Successfully synchronized 10 Mexico City spots (DB + Redis)!");
    }

    private void loadMexicoCityPlaces(City city) {
        savePlace(city, "Zocalo", "The main square of the city. Visit early morning to see the massive flag-raising ceremony.", 19.4326, -99.1332, "ZOCALO");
        savePlace(city, "Palacio de Bellas Artes", "Admire the stunning murals by Rivera and Siqueiros inside this white marble palace.", 19.4352, -99.1412, "BELLAS_ARTES");
        savePlace(city, "Frida Kahlo Museum", "Also known as the Blue House. Book tickets weeks in advance to see where Frida lived.", 19.3551, -99.1625, "FRIDA_KAHLO");
        savePlace(city, "Chapultepec Castle", "The only royal castle in North America, offering a panoramic view of the Reforma skyline.", 19.4204, -99.1818, "CHAPULTEPEC");
        savePlace(city, "Teotihuacan", "Explore the ancient Pyramid of the Sun and Moon. Wear sunblock and comfortable shoes.", 19.6925, -98.8435, "TEOTIHUACAN");
        savePlace(city, "Anthropology Museum", "Houses the Aztec Sun Stone. Dedicate at least 3 hours to see the vast pre-Hispanic collection.", 19.4260, -99.1862, "ANTHRO_MUSEUM");
        savePlace(city, "Coyoacán Market", "A colorful market where you can try tostadas and traditional Mexican snacks.", 19.3517, -99.1614, "COYOACAN_MARKET");
        savePlace(city, "Xochimilco", "Ride a colorful trajenera boat through the ancient canals with mariachi music.", 19.2556, -99.1033, "XOCHIMILCO");
        savePlace(city, "Basilica of Our Lady of Guadalupe", "One of the most visited Catholic pilgrimage sites in the world.", 19.4849, -99.1176, "GUADALUPE");
        savePlace(city, "Reforma Avenue", "Walk along the 'Angel of Independence' statue and enjoy the modern architectural marvels.", 19.4270, -99.1676, "REFORMA");
    }

    private void savePlace(City city, String name, String tip, double lat, double lng, String code) {
        if (photoPlaceRepository.findByPlaceCode(code).isPresent()) {
            log.info("⏩ Place {} already exists. Skipping.", code);
            return;
        }

        PhotoPlace place = PhotoPlace.createPhotoPlace(name, tip, city, code, lat, lng);
        photoPlaceRepository.saveAndFlush(place);

        redisTemplate.opsForGeo().add(
                GEO_KEY_PREFIX + city.getId(),
                new Point(lng, lat),
                code
        );

        log.info("✅ Place saved to DB & Redis: {} ({})", name, code);
    }
}
