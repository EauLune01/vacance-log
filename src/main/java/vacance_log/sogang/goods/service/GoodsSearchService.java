package vacance_log.sogang.goods.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vacance_log.sogang.diary.domain.DiaryType;
import vacance_log.sogang.diary.dto.command.DiaryQueryCommand;
import vacance_log.sogang.diary.dto.result.DiaryDetailResult;
import vacance_log.sogang.diary.service.DiaryService;
import vacance_log.sogang.global.exception.user.UserNotFoundException;
import vacance_log.sogang.global.service.OpenAiService;
import vacance_log.sogang.goods.dto.command.GoodsSearchCommand;
import vacance_log.sogang.goods.dto.result.GoodsSearchResult;
import vacance_log.sogang.room.repository.UserRoomRepository;
import vacance_log.sogang.user.domain.User;
import vacance_log.sogang.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsSearchService {

    private final VectorStore vectorStore;
    private final DiaryService diaryService;
    private final OpenAiService openAiService;
    private final UserRepository userRepository;
    private final UserRoomRepository userRoomRepository;

    public GoodsSearchResult searchAndChat(GoodsSearchCommand command) {
        User user = getUserOrThrow(command.getUserId());
        List<Long> myRoomIds = userRoomRepository.findAllByUserId(command.getUserId()).stream()
                .map(ur -> ur.getRoom().getId())
                .toList();

        if (myRoomIds.isEmpty()) {
            return GoodsSearchResult.of(String.format("I'm sorry, %s...", user.getNickname()), Collections.emptyList(), false);
        }

        String rawQuery = command.getQuery();
        String optimizedQuery = openAiService.extractSearchKeywords(rawQuery);

        String finalSearchQuery = StringUtils.hasText(optimizedQuery) ? optimizedQuery : rawQuery;
        log.info("🔍 [RAG Search] Final Query for Vector Store: [{}]", finalSearchQuery);

        Filter.Expression finalFilter = createFilter(command.getUserId(), myRoomIds);

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(finalSearchQuery)
                        .topK(3)
                        .similarityThreshold(0.5)
                        .filterExpression(finalFilter)
                        .build()
        );

        List<DiaryDetailResult> diaryResults = documents.stream()
                .map(doc -> {
                    Long roomId = Long.parseLong(doc.getMetadata().get("roomId").toString());
                    DiaryType type = DiaryType.valueOf(doc.getMetadata().get("type").toString());
                    Long diaryUserId = (type == DiaryType.INDIVIDUAL) ? command.getUserId() : null;
                    return diaryService.getDiaryDetail(DiaryQueryCommand.of(roomId, diaryUserId, type));
                })
                .distinct()
                .toList();

        String context = documents.isEmpty() ? "No related diary found." :
                documents.stream().map(Document::getText).collect(Collectors.joining("\n\n"));

        String aiAnswer = openAiService.generateAnswerFromDiaries(rawQuery, context, user.getNickname());

        return GoodsSearchResult.of(aiAnswer, diaryResults, !documents.isEmpty());
    }

    private Filter.Expression createFilter(Long userId, List<Long> myRoomIds) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        return b.group(
                b.or(
                        b.and(b.eq("userId", userId), b.eq("type", "INDIVIDUAL")),
                        b.and(b.in("roomId", myRoomIds.toArray()), b.eq("type", "GROUP"))
                )
        ).build();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
    }
}