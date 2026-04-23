package vacance_log.sogang.photo.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import vacance_log.sogang.photo.domain.Photo;
import vacance_log.sogang.photo.repository.custom.PhotoRepositoryCustom;

import java.util.List;
import java.util.Optional;

import static vacance_log.sogang.photo.domain.QPhoto.photo;
import static vacance_log.sogang.room.domain.QRoom.room;
import static vacance_log.sogang.user.domain.QUser.user;

@RequiredArgsConstructor
public class PhotoRepositoryImpl implements PhotoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Photo> findPhotosByPlace(Long roomId, Long placeId) {
        return queryFactory
                .selectFrom(photo)
                .join(photo.user).fetchJoin()
                .where(
                        photo.room.id.eq(roomId),
                        photo.photoPlace.id.eq(placeId)
                )
                .fetch();
    }

    @Override
    public Optional<Photo> findByIdDetail(Long photoId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(photo)
                        .join(photo.room, room).fetchJoin()
                        .join(room.city).fetchJoin()
                        .join(photo.user, user).fetchJoin()
                        .leftJoin(photo.photoPlace).fetchJoin()
                        .where(photo.id.eq(photoId))
                        .fetchOne()
        );
    }
}
