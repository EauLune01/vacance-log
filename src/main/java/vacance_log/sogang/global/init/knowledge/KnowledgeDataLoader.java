package vacance_log.sogang.global.init.knowledge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import vacance_log.sogang.rag.repository.TravelKnowledgeRepository;
import vacance_log.sogang.rag.service.TravelKnowledgeEtlService;

@Slf4j
@Component
@Order(10) // 모든 City, PhotoPlace, Room 생성이 끝난 후 실행
@RequiredArgsConstructor
public class KnowledgeDataLoader implements CommandLineRunner {

    private final TravelKnowledgeEtlService etlService;
    private final TravelKnowledgeRepository repository;

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() > 0) {
            log.info("⏩ Travel knowledge already exists. Skipping ETL.");
            return;
        }

        log.info("🚀 Starting Global Travel Knowledge ETL process...");

        etlService.importKnowledge("data/africa/morocco/marrakech_knowledge.json");
        etlService.importKnowledge("data/europe/france/paris_knowledge.json");
        etlService.importKnowledge("data/asia/japan/osaka_knowledge.json");
        etlService.importKnowledge("data/america/mexico/mexico_city_knowledge.json");

        log.info("✅ Global Travel Knowledge ETL completed successfully!");
    }
}
