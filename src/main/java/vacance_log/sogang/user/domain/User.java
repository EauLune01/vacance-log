package vacance_log.sogang.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vacance_log.sogang.global.domain.BaseEntity;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private LocalDate birthDate;

    private int age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Persona persona;

    public static User createUser(String name, String nickname, LocalDate birthDate, Gender gender, Persona persona) {
        User user = new User();
        user.name = name;
        user.nickname = nickname;
        user.birthDate = birthDate;
        user.gender = gender;
        user.persona = persona;
        user.age = LocalDate.now().getYear() - birthDate.getYear() + 1;
        return user;
    }
}