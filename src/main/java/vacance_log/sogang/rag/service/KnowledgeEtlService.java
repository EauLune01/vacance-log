package vacance_log.sogang.rag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeEtlService {

    private final VectorStore vectorStore;

    @Transactional
    public void etlMarrakechKnowledge() { // String -> void
        try {
            // 1. E: 추출
            Resource resource = new ClassPathResource("data/marrakech_knowledge.json");

            JsonReader jsonReader = new JsonReader(
                    resource,
                    jsonMap -> Map.of(
                            "placeCode", jsonMap.get("placeCode"),
                            "city", jsonMap.get("city"),
                            "theme", jsonMap.get("theme"),
                            "personas", jsonMap.get("personas")
                    ),
                    "content"
            );

            List<Document> documents = jsonReader.read();
            log.info("추출된 지식 Document 수: {} 개", documents.size());

            // 2. T: 변환
            TokenTextSplitter transformer = new TokenTextSplitter();
            List<Document> transformedDocuments = transformer.apply(documents);
            log.info("변환된 지식 Document 수: {} 개", transformedDocuments.size());

            // 3. L: 적재
            vectorStore.add(transformedDocuments);

            log.info("✅ 마라케시 RAG 지식 데이터 pgvector 적재 완료!");

        } catch (Exception e) {
            log.error("❌ ETL 과정 중 오류 발생: {}", e.getMessage());
        }
    }
}
