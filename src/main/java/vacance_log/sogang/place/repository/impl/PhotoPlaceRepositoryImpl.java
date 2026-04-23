package vacance_log.sogang.place.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import vacance_log.sogang.place.domain.QPhotoPlace;
import vacance_log.sogang.place.repository.custom.PhotoPlaceRepositoryCustom;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PhotoPlaceRepositoryImpl implements PhotoPlaceRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<String, String> findPlaceNamesByCodes(List<String> placeCodes) {
        QPhotoPlace photoPlace = QPhotoPlace.photoPlace;

        return queryFactory
                .select(photoPlace.placeCode, photoPlace.placeName)
                .from(photoPlace)
                .where(photoPlace.placeCode.in(placeCodes))
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(photoPlace.placeCode),
                        tuple -> tuple.get(photoPlace.placeName)
                ));
    }
}