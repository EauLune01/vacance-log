package vacance_log.sogang.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vacance_log.sogang.global.domain.BaseEntity;
import vacance_log.sogang.place.domain.PhotoPlace;
import vacance_log.sogang.room.domain.Room;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushHistory extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    private PhotoPlace photoPlace;

    @Column(columnDefinition = "TEXT")
    private String sentMessage;

    public static PushHistory createHistory(Room room, PhotoPlace place, String msg) {
        PushHistory history = new PushHistory();
        history.room = room;
        history.photoPlace = place;
        history.sentMessage = msg;
        return history;
    }
}
