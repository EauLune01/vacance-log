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
import vacance_log.sogang.diary.service.DiaryQueryService;
import vacance_log.sogang.global.exception.user.UserNotFoundException;
import vacance_log.sogang.global.service.OpenAiService;
import vacance_log.sogang.goods.dto.command.GoodsSearchCommand;
import vacance_log.sogang.goods.dto.result.GoodsSearchResult;
import vacance_log.sogang.room.repository.UserRoomRepository;
import vacance_log.sogang.user.domain.User;
import vacance_log.sogang.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
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
        String finalSearchQuery = StringUtils.hasText(optimizedQuery) ? optimizedQuery : rawQuery;

        Filter.Expression diaryFilter = createFilter(command.getUserId(), myRoomIds);
        FilterExpressionBuilder b = new FilterExpressionBuilder();

        // 1. 검색 범위 설정
        List<Document> allCandidates = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(finalSearchQuery)
                        .topK(5)
                        .similarityThreshold(0.65)
                        .filterExpression(diaryFilter)
                        .build()
        );

        // 2. 가장 유사도가 높은 문서의 도시를 기준으로 그룹핑
        List<Document> diaryDocs = new ArrayList<>();
        if (!allCandidates.isEmpty()) {
            // 1. 안전하게 첫 번째 문서의 도시명을 가져옴 (없으면 빈 문자열)
            Object cityObj = allCandidates.get(0).getMetadata().get("cityName");
            String targetCity = (cityObj != null) ? cityObj.toString() : "";

            if (!targetCity.isEmpty()) {
                // 2. 해당 도시와 일치하는 문서만 필터링
                diaryDocs = allCandidates.stream()
                        .filter(doc -> {
                            Object docCity = doc.getMetadata().get("cityName");
                            return docCity != null && docCity.toString().equals(targetCity);
                        })
                        .limit(2)
                        .toList();

                log.info("🎯 Selected City for Context: {}", targetCity);
            } else {
                diaryDocs = allCandidates.stream().limit(2).toList();
            }
        }
        // 3. 지식(KNOWLEDGE) 데이터 조회
        List<Document> knowledgeDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(finalSearchQuery)
                        .topK(2)
                        .similarityThreshold(0.6)
                        .filterExpression(b.eq("type", "KNOWLEDGE").build())
                        .build()
        );

        String userContext = diaryDocs.isEmpty() ? "No related diary found." :
                diaryDocs.stream().map(Document::getText).collect(Collectors.joining("\n\n"));

        String systemKnowledge = knowledgeDocs.isEmpty() ? "" :
                knowledgeDocs.stream().map(Document::getText).collect(Collectors.joining("\n\n"));

        // AI 답변 생성
        String aiAnswer = openAiService.generateAnswerFromDiaries(rawQuery, userContext, systemKnowledge, user.getNickname());

        // 4. 결과 매핑 (DiaryDetailResult 변환)
        List<DiaryDetailResult> diaryResults = diaryDocs.stream()
                .map(doc -> {
                    Long roomId = Long.parseLong(doc.getMetadata().get("roomId").toString());
                    DiaryType type = DiaryType.valueOf(doc.getMetadata().get("type").toString());

                    if (type == DiaryType.INDIVIDUAL) {
                        return diaryQueryService.getPersonalDiary(DiaryQueryCommand.of(roomId, command.getUserId(), type));
                    } else {
                        return diaryQueryService.getGroupDiary(roomId);
                    }
                })
                .distinct()
                .toList();

        return GoodsSearchResult.of(aiAnswer, diaryResults, !diaryDocs.isEmpty());
    }

    private Filter.Expression createFilter(Long userId, List<Long> myRoomIds) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();

        return b.group(
                b.or(
                        // 사진 메모 포함 내가 올린 모든 개인 데이터
                        b.and(
                                b.eq("userId", userId),
                                b.eq("type", "INDIVIDUAL")
                        ),

                        // 내가 속한 방의 그룹 다이어리
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