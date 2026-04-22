package vacance_log.sogang.diary.dto.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vacance_log.sogang.diary.domain.DiaryType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryQueryCommand {
    private Long roomId;
    private Long userId;
    private DiaryType type;

    public static DiaryQueryCommand of(Long roomId, Long userId, DiaryType type) {
        return DiaryQueryCommand.builder()
                .roomId(roomId)
                .userId(userId)
                .type(type)
                .build();
    }
}
