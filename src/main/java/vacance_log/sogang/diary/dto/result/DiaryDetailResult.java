package vacance_log.sogang.diary.dto.result;

import vacance_log.sogang.diary.domain.DiaryType;

public interface DiaryDetailResult {
    String getTitle();
    String getCityName();
    DiaryType getType();
}
