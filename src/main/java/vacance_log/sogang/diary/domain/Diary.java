package vacance_log.sogang.diary.domain;

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
public class Diary extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private DiaryType type;

    public static Diary createDiary(Room room, User user, String content, DiaryType type) {
        Diary diary = new Diary();
        room.getDiaries().add(diary);
        diary.room = room;
        diary.user = user;
        diary.content = content;
        diary.type = type;
        return diary;
    }
}
