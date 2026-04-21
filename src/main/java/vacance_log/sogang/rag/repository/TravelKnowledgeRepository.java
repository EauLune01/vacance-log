package vacance_log.sogang.rag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vacance_log.sogang.rag.domain.TravelKnowledge;

public interface TravelKnowledgeRepository extends JpaRepository<TravelKnowledge, Long> {
}
