package vacance_log.sogang.global.init;

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
@Order(2)
@RequiredArgsConstructor
public class MarrakechDataLoader implements CommandLineRunner {

    private final CityRepository cityRepository;
    private final PhotoPlaceRepository photoPlaceRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String GEO_KEY_PREFIX = "city:places:";

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (cityRepository.count() > 0) {
            log.info("⏩ Marrakech data already exists. Skipping data loading.");
            return;
        }

        City marrakech = City.createCity("Marrakech", "Morocco", 31.6295, -7.9811);
        cityRepository.save(marrakech);
        log.info("🌆 City created successfully: {}", marrakech.getName());

        savePlace(marrakech, "Jemaa el-Fnaa Square",
                "Capture the vibrant night market right after sunset with a wide-angle lens, and follow the aroma of grilled skewers.",
                31.6258, -7.9891, "JEMAA_EL_FNA");

        savePlace(marrakech, "Majorelle Garden",
                "Take portrait shots in front of the cobalt blue walls and relax along the bamboo pathways.",
                31.6417, -8.0033, "MAJORELLE");

        savePlace(marrakech, "Bahia Palace",
                "Explore the lavish rooms of 19th-century nobility and admire the intricate ceiling decorations.",
                31.6218, -7.9817, "BAHIA_PALACE");

        savePlace(marrakech, "Koutoubia Mosque",
                "Feel the historical essence of Marrakech around the 70m tall minaret.",
                31.6238, -7.9936, "KOUTOUBIA");

        savePlace(marrakech, "Ben Youssef Madrasa",
                "Discover the solemn beauty of this Islamic school and its geometric patterns.",
                31.6318, -7.9861, "BEN_YOUSSEF");

        savePlace(marrakech, "Souk Semmarine",
                "Enjoy the thrill of bargaining for leather bags, babouche shoes, and colorful lanterns.",
                31.6298, -7.9882, "SOUK_SMARINE");

        savePlace(marrakech, "Ensemble Artisanal",
                "If you're tired of bargaining, find high-quality handicrafts at fixed prices here.",
                31.6272, -7.9955, "ENSEMBLE_ART");

        savePlace(marrakech, "Al Fassia",
                "Taste the authentic tagine with lamb and prunes, a perfect balance of sweet and savory.",
                31.6361, -8.0083, "AL_FASSIA");

        savePlace(marrakech, "Naima Couscous",
                "On Friday lunch, enjoy a warm plate of couscous among locals.",
                31.6311, -7.9812, "NAIMA_COUSCOUS");

        savePlace(marrakech, "Le Jardin Secret",
                "Escape the noise of the medina and relax with mint tea on the garden terrace.",
                31.6305, -7.9885, "LE_JARDIN_SECRET");

        log.info("✅ Successfully synchronized 10 Marrakech spots (DB + Redis)!");
    }

    private void savePlace(City city, String name, String tip, double lat, double lng, String code) {
        PhotoPlace place = PhotoPlace.createPhotoPlace(name, tip, city, code, lat, lng);
        photoPlaceRepository.save(place);

        redisTemplate.opsForGeo().add(
                GEO_KEY_PREFIX + city.getId(),
                new Point(lng, lat),
                code
        );

        log.info("✅ Place saved to DB & Redis: {} ({})", name, code);
    }
}