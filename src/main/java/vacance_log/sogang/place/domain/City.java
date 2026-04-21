package vacance_log.sogang.place.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vacance_log.sogang.global.domain.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class City extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private double centerLat;
    @Column(nullable = false)
    private double centerLng;

    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL)
    private List<PhotoPlace> photoPlaces = new ArrayList<>();

    public static City createCity(String name, String country, double lat, double lng) {
        City city = new City();
        city.name = name;
        city.country = country;
        city.centerLat = lat;
        city.centerLng = lng;
        return city;
    }
}