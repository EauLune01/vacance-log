package vacance_log.sogang.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Persona {
    PHOTO_TRAVELER("photo traveler", "사진과 기록에 진심인 타입"),
    FOODIE("foodie", "맛집과 로컬 음식을 사랑하는 타입"),
    EXPLORER("explorer", "유적지와 모험을 즐기는 타입"),
    RELAXER("relaxer", "여유로운 휴식과 힐링을 선호하는 타입"),
    SHOPPER("shopper", "쇼핑과 트렌디한 장소를 좋아하는 타입");

    private final String englishName; // AI 프롬프트용
    private final String description; // 한국어 설명
}