package com.jh.emotion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jh.emotion.dto.EmotionAnalysisResultDto;
import com.jh.emotion.entity.DiaryRecord;
import com.jh.emotion.entity.User;
import com.jh.emotion.repository.DiaryRecordRepository;
import com.jh.emotion.repository.EmotionDailyStatRepository;
import com.jh.emotion.repository.UserPreferenceRepository;
import com.jh.emotion.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AiEmotionAnalysisServiceTest {

    @Mock private DiaryRecordRepository diaryRecordRepository;
    @Mock private UserPreferenceRepository userPreferenceRepository;
    @Mock private UserRepository userRepository;
    @Mock private CachingService cachingService;
    @Mock private RecommendationService recommendationService;
    @Mock private EmotionDailyStatRepository emotionDailyStatRepository;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private WebClient webClient;
    
    @Spy private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AiEmotionAnalysisService aiEmotionAnalysisService;

    private User testUser;
    private DiaryRecord testDiary;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);

        testDiary = new DiaryRecord();
        testDiary.setDiaryRecordId(100L);
        testDiary.setUser(testUser);
        testDiary.setContent("오늘 하루는 정말 기뻤다.");
    }

    @Test
    @DisplayName("중립 감정 비율이 20%를 초과하면 20%로 조정되고 나머지 감정에 분배되어야 함")
    void adjustNeutralEmotionRatioTest() throws Exception {
        // Given: AI 분석 결과가 '중립' 80%, '기쁨' 20%인 상황
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode emotions = root.putArray("emotions");
        
        emotions.addObject().put("label", "중립").put("ratio", 0.8).put("level", 5).put("confidence", 0.9).put("description", "평범함");
        emotions.addObject().put("label", "기쁨").put("ratio", 0.2).put("level", 8).put("confidence", 0.9).put("description", "즐거움");
        root.put("comment", "좋은 하루네요.");

        when(diaryRecordRepository.findById(anyLong())).thenReturn(Optional.of(testDiary));

        // When
        EmotionAnalysisResultDto result = aiEmotionAnalysisService.saveEmotionAnalysisResult(100L, root, "someHash");

        // Then: 중립은 0.2로 고정, 기쁨은 나머지 0.8을 다 가져가야 함 (0.2 -> 0.8)
        assertThat(result.getEmotions()).hasSize(2);
        
        result.getEmotions().forEach(e -> {
            if (e.getLabel().equals("중립")) {
                assertThat(e.getRatio()).isEqualTo(0.20);
            } else if (e.getLabel().equals("기쁨")) {
                assertThat(e.getRatio()).isEqualTo(0.80);
            }
        });
    }

    @Test
    @DisplayName("중립 감정이 이미 20% 이하일 경우 비율이 조정되지 않아야 함")
    void lowNeutralEmotionTest() throws Exception {
        // Given: 중립 10%, 기쁨 90%
        ObjectNode root = objectMapper.createObjectNode();
        ArrayNode emotions = root.putArray("emotions");
        emotions.addObject().put("label", "중립").put("ratio", 0.1).put("level", 2).put("confidence", 0.9).put("description", "평범");
        emotions.addObject().put("label", "기쁨").put("ratio", 0.9).put("level", 9).put("confidence", 0.9).put("description", "행복");
        root.put("comment", "좋네요.");

        when(diaryRecordRepository.findById(anyLong())).thenReturn(Optional.of(testDiary));

        // When
        EmotionAnalysisResultDto result = aiEmotionAnalysisService.saveEmotionAnalysisResult(100L, root, "someHash");

        // Then: 0.1 그대로 유지
        result.getEmotions().forEach(e -> {
            if (e.getLabel().equals("중립")) {
                assertThat(e.getRatio()).isEqualTo(0.1);
            }
        });
    }
}
