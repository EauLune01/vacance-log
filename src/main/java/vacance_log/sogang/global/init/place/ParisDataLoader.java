package vacance_log.sogang.global.init.place;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import vacance_log.sogang.place.domain.City;
import vacance_log.sogang.place.domain.PhotoPlace;
import vacance_log.sogang.place.repository.CityRepository;
import vacance_log.sogang.place.repository.PhotoPlaceRepository;

@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class ParisDataLoader implements CommandLineRunner {

    private final CityRepository cityRepository;
    private final PhotoPlaceRepository photoPlaceRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String GEO_KEY_PREFIX = "city:places:";

    @Override
    public void run(String... args) throws Exception {
        if (cityRepository.findByName("Paris").isPresent()) {
            log.info("⏩ Paris data already exists. Skipping.");
            return;
        }

        City paris = City.createCity("Paris", "France", 48.8566, 2.3522);
        cityRepository.saveAndFlush(paris);
        log.info("🌆 City created: {}", paris.getName());

        loadParisPlaces(paris);

        log.info("✅ Successfully synchronized 10 Paris spots (DB + Redis)!");
    }

    private void loadParisPlaces(City city) {
        savePlace(city, "Louvre Museum", "Check the Denon wing for the Mona Lisa and use the Pyramid entrance early morning.", 48.8606, 2.3376, "LOUVRE");
        savePlace(city, "Orsay Museum", "Famous for Impressionist masterpieces. The clock window offers a great photo spot.", 48.8599, 2.3265, "ORSAY");
        savePlace(city, "Eiffel Tower", "Best view is from Trocadéro. Don't miss the sparkling lights at the start of every hour.", 48.8584, 2.2945, "EIFFEL");
        savePlace(city, "Le Vrai Paris", "A charming cafe in Montmartre with beautiful flower decorations.", 48.8824, 2.3385, "LE_VRAI_PARIS");
        savePlace(city, "Arc de Triomphe", "Walk through the underground passage to reach it. The rooftop view is amazing.", 48.8738, 2.2950, "ARC_DE_TRIOMPHE");
        savePlace(city, "Luxembourg Gardens", "Perfect for a picnic or a quiet stroll. Watch locals play sailboats in the pond.", 48.8462, 2.3371, "LUXEMBOURG");
        savePlace(city, "Notre-Dame Cathedral", "The heart of Paris. Admire the gothic architecture and the Point Zéro marker.", 48.8529, 2.3500, "NOTRE_DAME");
        savePlace(city, "Sacré-Cœur Basilica", "Located at the highest point. Catch the sunset over the city from the white domes.", 48.8867, 2.3431, "SACRE_COEUR");
        savePlace(city, "Galeries Lafayette", "Don't miss the stunning glass dome and the free rooftop terrace view.", 48.8737, 2.3320, "LAFAYETTE");
        savePlace(city, "Musée Marmottan Monet", "A hidden gem for Monet fans, housing the 'Impression, Sunrise' painting.", 48.8591, 2.2677, "MONET_MUSEUM");
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
