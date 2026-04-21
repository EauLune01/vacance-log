package vacance_log.sogang.room.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vacance_log.sogang.diary.domain.Diary;
import vacance_log.sogang.global.domain.BaseEntity;
import vacance_log.sogang.notification.domain.PushHistory;
import vacance_log.sogang.global.exception.room.RoomCapacityExceededException;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.place.domain.City;
import vacance_log.sogang.user.domain.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRoom> userRooms = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Photo> photos = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Diary> diaries = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<PushHistory> pushHistories = new ArrayList<>();

    private static final int MAX_CAPACITY = 4;

    public static Room createRoom(String title, City city) {
        Room room = new Room();
        room.title = title;
        room.city = city;
        room.status = RoomStatus.ONGOING;
        return room;
    }

    public void addParticipant(User user, ParticipantRole role) {
        validateCapacity();

        UserRoom userRoom = UserRoom.createUserRoom(user, this, role);
        this.userRooms.add(userRoom);
    }

    public void finishTravel() {
        this.status = RoomStatus.FINISHED;
    }

    private void validateCapacity() {
        if (isFull()) {
            throw new RoomCapacityExceededException(
                    "여행 인원은 최대 " + MAX_CAPACITY + "명까지 가능합니다."
            );
        }
    }

    private boolean isFull() {
        return this.userRooms.size() >= MAX_CAPACITY;
    }
}