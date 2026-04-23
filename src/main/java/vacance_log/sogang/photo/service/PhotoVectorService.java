package vacance_log.sogang.photo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import vacance_log.sogang.photo.domain.Photo;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoVectorService {

    private final VectorStore vectorStore;

    public void upsert(Photo photo, String content) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("photoId", photo.getId());
        metadata.put("roomId", photo.getRoom().getId());
        metadata.put("userId", photo.getUser().getId());
        metadata.put("type", "PHOTO");

        log.info("📤 [Vector DB] Upserting Photo Embedding - ID: {}", photo.getId());

        Document document = new Document(content, metadata);
        vectorStore.add(List.of(document));
    }
}
