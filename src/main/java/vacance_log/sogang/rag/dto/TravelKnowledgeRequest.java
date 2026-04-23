package vacance_log.sogang.rag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelKnowledgeRequest {
    private String content;
    private String city;
    private String placeCode;
    private String theme;
    private String timeTag;
    private List<String> personas;
}