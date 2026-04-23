package vacance_log.sogang.goods.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vacance_log.sogang.goods.dto.command.GoodsSearchCommand;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GoodsSearchRequest {
    @NotNull(message = "질문 내용은 필수입니다.")
    @NotEmpty(message = "질문 내용을 입력해주세요.")
    private String query;

    public GoodsSearchCommand toCommand(Long userId) {
        return GoodsSearchCommand.of(userId, this.query);
    }
}
