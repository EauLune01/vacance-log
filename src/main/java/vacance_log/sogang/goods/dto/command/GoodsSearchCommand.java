package vacance_log.sogang.goods.dto.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoodsSearchCommand {
    private Long userId;
    private String query;

    public static GoodsSearchCommand of(Long userId, String query) {
        return new GoodsSearchCommand(userId, query);
    }
}
