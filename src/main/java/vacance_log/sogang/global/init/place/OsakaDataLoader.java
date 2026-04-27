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
@Order(4)
@RequiredArgsConstructor
public class OsakaDataLoader implements CommandLineRunner {

    private final CityRepository cityRepository;
    private final PhotoPlaceRepository photoPlaceRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String GEO_KEY_PREFIX = "city:places:";

    @Override
    public void run(String... args) throws Exception {
        if (cityRepository.findByName("Osaka").isPresent()) {
            log.info("⏩ Osaka data already exists. Skipping.");
            return;
        }

        City osaka = City.createCity("Osaka", "Japan", 34.6937, 135.5023);
        cityRepository.saveAndFlush(osaka);
        log.info("🌆 City created: {}", osaka.getName());

        loadOsakaPlaces(osaka);

        log.info("✅ Successfully synchronized 10 Osaka spots (DB + Redis)!");
    }

    private void loadOsakaPlaces(City city) {
        savePlace(city, "Expo '70 Commemorative Park", "Visit the Tower of the Sun and enjoy the seasonal flower gardens.", 34.8061, 135.5305, "EXPO_70_PARK");
        savePlace(city, "Umeda Sky Building", "The Kuchu Teien Observatory offers a 360-degree open-air view of Osaka.", 34.7053, 135.4906, "UMEDA_SKY");
        savePlace(city, "Osaka Castle", "A symbol of Osaka history. The surrounding park is especially beautiful during cherry blossom season.", 34.6873, 135.5262, "OSAKA_CASTLE");
        savePlace(city, "Glico Sign", "The must-visit photo spot in Dotonbori. Strike the famous running pose!", 34.6691, 135.5013, "GLICO_SIGN");
        savePlace(city, "Namba Parks", "An urban oasis with rooftop gardens. Great for shopping and architecture photography.", 34.6616, 135.5020, "NAMBA_PARKS");
        savePlace(city, "Kuromon Ichiba Market", "Osaka's kitchen. Try fresh seafood, especially grilled scallops and uni.", 34.6654, 135.5057, "KUROMON_MARKET");
        savePlace(city, "Namba Yasaka Shrine", "The lion-head shaped stage is incredibly unique and powerful for photos.", 34.6613, 135.4965, "NAMBA_YASAKA");
        savePlace(city, "Tsutenkaku Tower", "The nostalgic landmark of Shinsekai. Touch Billiken's feet for good luck.", 34.6525, 135.5063, "TSUTENKAKU");
        savePlace(city, "Kushikatsu Benkei", "Authentic Shinsekai kushikatsu experience. Remember: No double dipping!", 34.6508, 135.5050, "KUSHIKATSU_BENKEI");
        savePlace(city, "Torato-izakaya Torayanke", "A cozy local pub for Hanshin Tigers fans in Namba. Great for ending the night with yakitori and sake.", 34.6661, 135.5005, "TORAYANKE");
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