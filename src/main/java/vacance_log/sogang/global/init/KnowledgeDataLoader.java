package vacance_log.sogang.global.init;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import vacance_log.sogang.rag.domain.TravelKnowledge;
import vacance_log.sogang.rag.repository.TravelKnowledgeRepository;
import vacance_log.sogang.rag.service.KnowledgeEtlService;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3)
public class KnowledgeDataLoader implements CommandLineRunner {

    private final TravelKnowledgeRepository knowledgeRepository;
    private final KnowledgeEtlService etlService;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        if (knowledgeRepository.count() == 0) {
            log.info("Data initialization required. Starting JPA and Vector Store loading...");
            initJpaData();
            etlService.etlMarrakechKnowledge();
        } else {
            log.info("Data already exists. Skipping initialization logic.");
        }
    }

    private void initJpaData() throws IOException {
        Resource resource = new ClassPathResource("data/marrakech_knowledge.json");

        List<Map<String, Object>> rawData = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<Map<String, Object>>>() {}
        );

        List<TravelKnowledge> entities = rawData.stream()
                .map(map -> TravelKnowledge.createKnowledge(
                        (String) map.get("content"),
                        (String) map.get("city"),
                        (String) map.get("placeCode"),
                        (String) map.get("theme"),
                        (String) map.get("timeTag"),
                        (List<String>) map.get("personas")
                ))
                .collect(Collectors.toList());

        knowledgeRepository.saveAll(entities);
        log.info("Successfully saved {} TravelKnowledge entities to the database.", entities.size());
    }
}