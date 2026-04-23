package vacance_log.sogang.rag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vacance_log.sogang.rag.domain.TravelKnowledge;
import java.util.List;
import java.util.Optional;

public interface TravelKnowledgeRepository extends JpaRepository<TravelKnowledge, Long> {

    @Query("select t from TravelKnowledge t where t.placeCode in :codes")
    List<TravelKnowledge> findByCodes(@Param("codes") List<String> codes);

    @Query("select t from TravelKnowledge t where t.placeCode = :code")
    Optional<TravelKnowledge> findByCode(@Param("code") String code);

    List<TravelKnowledge> findAllByCity(String city);
}
