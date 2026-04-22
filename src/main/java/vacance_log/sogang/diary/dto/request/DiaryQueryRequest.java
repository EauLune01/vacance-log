package vacance_log.sogang.diary.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import vacance_log.sogang.diary.domain.DiaryType;
import vacance_log.sogang.diary.dto.command.DiaryQueryCommand;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryQueryRequest {

    @NotNull(message = "방 ID는 필수입니다.")
    private Long roomId;

    private Long userId;

    @NotNull(message = "다이어리 타입은 필수입니다.")
    private DiaryType type;

    public static DiaryQueryCommand toCommand(DiaryQueryRequest request) {
        return DiaryQueryCommand.builder()
                .roomId(request.getRoomId())
                .userId(request.getUserId())
                .type(request.getType())
                .build();
    }
}
