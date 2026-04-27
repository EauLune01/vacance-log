package vacance_log.sogang.goods.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vacance_log.sogang.diary.domain.DiaryType;
import vacance_log.sogang.diary.dto.command.DiaryQueryCommand;
import vacance_log.sogang.diary.dto.result.DiaryDetailResult;
import vacance_log.sogang.diary.service.DiaryQueryService;
import vacance_log.sogang.global.exception.user.UserNotFoundException;
import vacance_log.sogang.global.service.OpenAiService;
import vacance_log.sogang.goods.dto.command.GoodsSearchCommand;
import vacance_log.sogang.goods.dto.result.GoodsSearchResult;
import vacance_log.sogang.room.repository.UserRoomRepository;
import vacance_log.sogang.user.domain.User;
import vacance_log.sogang.user.repository.UserRepository;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoodsSearchService {

    private final VectorStore vectorStore;
    private final DiaryQueryService diaryQueryService;
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

        Filter.Expression diaryFilter = createFilter(command.getUserId(), myRoomIds);
        FilterExpressionBuilder b = new FilterExpressionBuilder();

        List<Document> allCandidates = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(optimizedQuery)
                        .topK(10)
                        .similarityThreshold(0.5)
                        .filterExpression(diaryFilter)
                        .build()
        );

        List<Document> diaryDocs = allCandidates.stream()
                .filter(doc -> {
                    Object cityObj = doc.getMetadata().get("cityName");
                    if (cityObj == null) return false;
                    String cityName = cityObj.toString();

                    return optimizedQuery.toLowerCase().contains(cityName.toLowerCase()) ||
                            cityName.toLowerCase().contains(optimizedQuery.toLowerCase());
                })
                .collect(Collectors.toMap(
                        doc -> String.valueOf(doc.getMetadata().get("cityName")) + "_" + String.valueOf(doc.getMetadata().get("type")),
                        doc -> doc,
                        (existing, replacement) -> existing
                ))
                .values().stream()
                .sorted(Comparator.comparing(doc -> String.valueOf(doc.getMetadata().get("type")))) // 정렬 보장
                .limit(2)
                .toList();

        List<Document> knowledgeDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(optimizedQuery)
                        .topK(2)
                        .filterExpression(b.eq("city", optimizedQuery).build())
                        .build()
        );

        String userContext = diaryDocs.isEmpty() ? "No related diary found." :
                diaryDocs.stream().map(Document::getText).collect(Collectors.joining("\n\n"));

        String systemKnowledge = knowledgeDocs.isEmpty() ? "" :
                knowledgeDocs.stream().map(Document::getText).collect(Collectors.joining("\n\n"));

        String aiAnswer = openAiService.generateAnswerFromDiaries(rawQuery, userContext, systemKnowledge, user.getNickname());

        List<DiaryDetailResult> diaryResults = diaryDocs.stream()
                .map(doc -> {
                    Long roomId = Long.parseLong(doc.getMetadata().get("roomId").toString());
                    DiaryType type = DiaryType.valueOf(doc.getMetadata().get("type").toString());

                    return (type == DiaryType.INDIVIDUAL) ?
                            diaryQueryService.getPersonalDiary(DiaryQueryCommand.of(roomId, command.getUserId(), type)) :
                            diaryQueryService.getGroupDiary(roomId);
                })
                .toList();

        return GoodsSearchResult.of(aiAnswer, diaryResults, !diaryDocs.isEmpty());
    }

    private Filter.Expression createFilter(Long userId, List<Long> myRoomIds) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();

        return b.group(
                b.or(
                        // 내 개인 다이어리 데이터
                        b.and(
                                b.eq("userId", userId),
                                b.eq("type", "INDIVIDUAL")
                        ),
                        // 내가 참여한 방의 그룹 다이어리 데이터
                        b.and(
                                b.in("roomId", myRoomIds.toArray()),
                                b.eq("type", "GROUP")
                        )
                )
        ).build();
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
    }
}