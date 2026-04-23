package vacance_log.sogang.rag.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vacance_log.sogang.rag.domain.TravelKnowledge;
import vacance_log.sogang.rag.dto.TravelKnowledgeRequest;
import vacance_log.sogang.rag.repository.TravelKnowledgeRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TravelKnowledgeEtlService {

    private final TravelKnowledgeRepository travelKnowledgeRepository;
    private final VectorStore vectorStore;
    private final ObjectMapper objectMapper;

    @Transactional
    public void importKnowledge(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            List<TravelKnowledgeRequest> requests = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<TravelKnowledgeRequest>>() {}
            );

            List<TravelKnowledge> entities = requests.stream()
                    .map(req -> TravelKnowledge.createKnowledge(
                            req.getContent(), req.getCity(), req.getPlaceCode(),
                            req.getTheme(), req.getTimeTag(), req.getPersonas()))
                    .toList();

            travelKnowledgeRepository.saveAll(entities);

            // 🚀 Vector Store 적재
            List<Document> documents = entities.stream()
                    .map(entity -> new Document(
                            entity.getContent(),
                            Map.of(
                                    "city", entity.getCity(),
                                    "placeCode", entity.getPlaceCode(),
                                    "type", "KNOWLEDGE"
                            )
                    ))
                    .toList();

            vectorStore.add(documents);
            log.info("✅ Successfully loaded {} knowledge items into DB & VectorStore: {}", entities.size(), resourcePath);

        } catch (IOException e) {
            log.error("❌ Failed to load knowledge JSON [{}]: {}", resourcePath, e.getMessage());
        }
    }
}
