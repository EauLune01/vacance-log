package vacance_log.sogang.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import vacance_log.sogang.diary.domain.Diary;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryVectorService {
    private final VectorStore vectorStore;

    public void upsert(Diary diary) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("roomId", diary.getRoom().getId());
        metadata.put("type", "GROUP");
        metadata.put("userId", -1L);
        metadata.put("cityName", diary.getRoom().getCity().getName());

        Document document = new Document(diary.getContent(), metadata);
        vectorStore.add(List.of(document));
    }
}
