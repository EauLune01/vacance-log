package vacance_log.sogang.place.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vacance_log.sogang.global.domain.BaseEntity;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PhotoPlace extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String placeName;

    @Column(columnDefinition = "TEXT")
    private String recommendTip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    private String placeCode;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    public static PhotoPlace createPhotoPlace(String placeName, String recommendTip, City city, String placeCode, double lat, double lng) {
        PhotoPlace photoPlace = new PhotoPlace();
        photoPlace.placeName = placeName;
        photoPlace.recommendTip = recommendTip;
        photoPlace.city = city;
        photoPlace.placeCode = placeCode;
        photoPlace.lat = lat;
        photoPlace.lng = lng;
        return photoPlace;
    }
}
