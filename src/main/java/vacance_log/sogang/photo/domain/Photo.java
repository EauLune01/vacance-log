package vacance_log.sogang.photo.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vacance_log.sogang.global.domain.BaseEntity;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.user.domain.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Photo extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String s3Url;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String landmarkName;
    private String groupTag;

    public static Photo createPhoto(String s3Url, User user, Room room, String landmarkName, String groupTag) {
        Photo photo = new Photo();
        photo.s3Url = s3Url;
        photo.user = user;
        photo.room = room;
        photo.landmarkName = landmarkName;
        photo.groupTag = groupTag;
        return photo;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}
