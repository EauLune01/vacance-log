package vacance_log.sogang.diary.repository.impl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import vacance_log.sogang.diary.domain.DiaryType;
import vacance_log.sogang.diary.repository.custom.DiaryRepositoryCustom;
import vacance_log.sogang.room.domain.Room;
import vacance_log.sogang.user.domain.User;

import java.util.Optional;

import static vacance_log.sogang.diary.domain.QDiary.diary;

@RequiredArgsConstructor
public class DiaryRepositoryImpl implements DiaryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<String> findContent(Room room, DiaryType type, User user) {
        return Optional.ofNullable(
                queryFactory
                        .select(diary.content)
                        .from(diary)
                        .where(
                                diary.room.eq(room),
                                diary.type.eq(type),
                                eqUser(user)
                        )
                        .fetchOne()
        );
    }

    private BooleanExpression eqUser(User user) {
        return user != null ? diary.user.eq(user) : null;
    }

}
