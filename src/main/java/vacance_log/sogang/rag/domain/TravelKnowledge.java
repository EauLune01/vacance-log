package vacance_log.sogang.rag.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vacance_log.sogang.global.domain.BaseEntity;

import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelKnowledge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String placeCode;

    private String theme;
    private String timeTag;

    @ElementCollection
    @CollectionTable(name = "knowledge_personas", joinColumns = @JoinColumn(name = "knowledge_id"))
    private List<String> personas = new ArrayList<>();

    public static TravelKnowledge createKnowledge(String content, String city, String placeCode,
                                                  String theme, String timeTag, List<String> personas) {
        TravelKnowledge knowledge = new TravelKnowledge();
        knowledge.content = content;
        knowledge.city = city;
        knowledge.placeCode = placeCode;
        knowledge.theme = theme;
        knowledge.timeTag = timeTag;
        knowledge.personas = personas;
        return knowledge;
    }
}
