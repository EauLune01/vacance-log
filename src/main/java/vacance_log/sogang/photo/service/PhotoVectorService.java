package vacance_log.sogang.photo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import vacance_log.sogang.photo.domain.Photo;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoVectorService {

    private final VectorStore vectorStore;

    public void upsert(Photo photo, String content) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("photoId", photo.getId());
        metadata.put("roomId", photo.getRoom().getId());
        metadata.put("userId", photo.getUser().getId());
        metadata.put("type", "INDIVIDUAL");
        metadata.put("cityName", photo.getRoom().getCity().getName());

        Document document = new Document(content, metadata);
        vectorStore.add(List.of(document));
    }
}