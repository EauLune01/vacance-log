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
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private DiaryType type;

    @Column(columnDefinition = "vector(1536)")
    private float[] embedding;

    // 개인 다이어리 생성 팩토리
    public static Diary createPersonal(Room room, User user) {
        Diary diary = new Diary();
        diary.room = room;
        diary.user = user;
        diary.type = DiaryType.INDIVIDUAL;
        return diary;
    }

    // 전체 에세이 생성 팩토리
    public static Diary createEssay(Room room) {
        Diary diary = new Diary();
        diary.room = room;
        diary.type = DiaryType.GROUP;
        return diary;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
}
