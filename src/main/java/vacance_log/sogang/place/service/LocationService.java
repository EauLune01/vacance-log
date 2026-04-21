package vacance_log.sogang.place.service;
import vacance_log.sogang.global.config.rabbitMq.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vacance_log.sogang.global.exception.room.RoomNotFoundException;
import vacance_log.sogang.place.dto.event.LocationEvent;
import vacance_log.sogang.place.dto.event.PlaceCandidate;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.room.repository.RoomRepository;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final RoomRepository roomRepository;

    private static final String GEO_KEY_PREFIX = "city:places:";
    private static final String NOTIFY_KEY_PREFIX = "notify:last:";
    private static final double SEARCH_RADIUS_KM = 0.2;
    private static final int MAX_CANDIDATES = 3;
    private static final Duration NOTIFY_TTL = Duration.ofMinutes(5);

    /**
     * Process user location update
     */
    public void processLocationUpdate(Long roomId, Double lat, Double lng) {
        Room room = getRoomOrThrow(roomId);

        List<PlaceCandidate> rawCandidates = detectNearbyPlaces(room, lat, lng);

        List<PlaceCandidate> validCandidates = filterValidCandidates(room, rawCandidates);

        if (shouldSkipRecommendation(roomId, validCandidates)) {
            return;
        }

        publishAndTrack(roomId, validCandidates, lat, lng);
    }

    /* =========================
       Core Logic
    ========================= */

    private List<PlaceCandidate> detectNearbyPlaces(Room room, Double lat, Double lng) {
        String key = GEO_KEY_PREFIX + room.getCity().getId();

        GeoResults<RedisGeoCommands.GeoLocation<Object>> results =
                searchNearbyPlaces(key, lat, lng);

        if (results.getContent().isEmpty()) {
            log.debug("📍 No nearby candidates found (roomId: {})", room.getId());
            return Collections.emptyList();
        }

        return results.getContent().stream()
                .map(r -> new PlaceCandidate(
                        r.getContent().getName().toString(),
                        Math.round(r.getDistance().getValue() * 1000)
                ))
                .toList();
    }

    private List<PlaceCandidate> filterValidCandidates(Room room, List<PlaceCandidate> candidates) {
        return candidates.stream()
                .filter(c -> !isAllMembersUploaded(room, c.getPlaceCode()))
                .toList();
    }

    private GeoResults<RedisGeoCommands.GeoLocation<Object>> searchNearbyPlaces(String key, Double lat, Double lng) {
        RedisGeoCommands.GeoSearchCommandArgs args = RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs()
                .includeDistance()
                .sortAscending()
                .limit(MAX_CANDIDATES);

        return redisTemplate.opsForGeo().search(
                key,
                GeoReference.fromCoordinate(lng, lat),
                new Distance(SEARCH_RADIUS_KM, Metrics.KILOMETERS),
                args
        );
    }

    /* =========================
       Event Handling
    ========================= */

    private void publishAndTrack(Long roomId, List<PlaceCandidate> candidates, Double lat, Double lng) {
        publishLocationEvent(roomId, candidates, lat, lng);

        String firstPlace = candidates.getFirst().getPlaceCode();
        saveLastNotification(roomId, firstPlace);

        log.info("📍 Recommendation completed (roomId: {}, place: {})", roomId, firstPlace);
    }

    private void publishLocationEvent(Long roomId, List<PlaceCandidate> candidates, Double lat, Double lng) {
        LocationEvent event = LocationEvent.of(roomId, candidates, lat, lng);

        rabbitTemplate.convertAndSend(
                RabbitMqConfig.TRAVEL_EXCHANGE,
                RabbitMqConfig.LOCATION_DETECTED,
                event
        );

        log.info("🚀 Published event with {} candidates (roomId: {})", candidates.size(), roomId);
    }

    /* =========================
       Validation & Policy
    ========================= */

    private boolean shouldSkipRecommendation(Long roomId, List<PlaceCandidate> candidates) {

        if (candidates.isEmpty()) {
            log.info("📸 Skipping recommendation - No valid candidates (roomId: {})", roomId);
            return true;
        }

        String firstPlace = candidates.getFirst().getPlaceCode();

        if (isRecentlyNotified(roomId, firstPlace)) {
            log.info("⚠️ Skipping recommendation - Recently notified for the same place (roomId: {}, place: {})", roomId, firstPlace);
            return true;
        }

        return false;
    }

    private boolean isAllMembersUploaded(Room room, String placeCode) {
        int totalMembers = room.getUserRooms().size();

        long uploadedUserCount = room.getPhotos().stream()
                .filter(photo -> placeCode.equals(photo.getLandmarkName()))
                .map(photo -> photo.getUser().getId())
                .distinct()
                .count();

        return uploadedUserCount >= totalMembers;
    }

    private void saveLastNotification(Long roomId, String placeCode) {
        String key = NOTIFY_KEY_PREFIX + roomId;
        redisTemplate.opsForValue().set(key, "\"" + placeCode + "\"", NOTIFY_TTL);
    }

    private boolean isRecentlyNotified(Long roomId, String placeCode) {
        String key = NOTIFY_KEY_PREFIX + roomId;
        try {
            Object lastPlace = redisTemplate.opsForValue().get(key);
            return placeCode.equals(lastPlace);
        } catch (Exception e) {
            redisTemplate.delete(key);
            return false;
        }
    }

    /* =========================
       Repository
    ========================= */

    private Room getRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found."));
    }
}
