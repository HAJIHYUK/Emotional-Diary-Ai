package com.jh.emotion.service;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.emotion.dto.EmotionStatsDto;
import com.jh.emotion.dto.EmotionStatsResponseDto;
import com.jh.emotion.repository.EmotionDailyStatRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmotionStatisticService {

    // EmotionRepository 대신 EmotionDailyStatRepository를 사용합니다.
    private final EmotionDailyStatRepository emotionDailyStatRepository;

    /**
     * 기간별 감정 통계를 조회합니다. 
     * EmotionDailyStat 테이블의 요약 데이터를 사용하여 빠르고 효율적으로 계산합니다.
     */
    public EmotionStatsResponseDto getEmotionStats(Long userId, LocalDate startDate, LocalDate endDate, String periodType) {
        
        // --- ▼ [로그 추가] 1. 메서드 시작 시 파라미터 확인 ▼ ---
        log.info("통계 조회 시작: userId={}, startDate={}, endDate={}, periodType={}", userId, startDate, endDate, periodType);
        // --- ▲ [로그 추가] 여기까지 ▲ ---

        List<Object[]> emotionStats = emotionDailyStatRepository.getEmotionStatsForPeriod(userId, startDate, endDate);

        // --- ▼ [로그 추가] 2. DB 쿼리 결과 확인 ▼ ---
        log.info("DB 조회 결과 (row 수): {}", emotionStats.size());
        emotionStats.forEach(row -> log.info("  - Row data: {}", Arrays.toString(row)));
        // --- ▲ [로그 추가] 여기까지 ▲ ---

        long totalEmotions = emotionStats.stream()
            .mapToLong(row -> (row[1] != null) ? ((Number) row[1]).longValue() : 0L) // 안전한 타입 변환
            .sum();

    // 3. 데이터가 없는 경우 처리
    if (totalEmotions == 0) {
        String emptyPeriodLabel = createPeriodLabel(startDate, periodType);
        return new EmotionStatsResponseDto(periodType, emptyPeriodLabel, new ArrayList<>(), "데이터 없음", "해당 기간에 분석된 감정이 없어요.");
    }

    // 4. DTO 리스트 및 최빈값 찾기
    List<EmotionStatsDto> dtos = new ArrayList<>();
    String topEmotion = "";
    long maxCount = 0L;

    for (Object[] row : emotionStats) {
        String emotionLabel = (String) row[0];
        

        long count = (row[1] != null) ? ((Number) row[1]).longValue() : 0L;
        long levelSum = (row[2] != null) ? ((Number) row[2]).longValue() : 0L;


        double avgLevel = (count > 0) ? (double) levelSum / count : 0.0;
        double totalRatio = (double) count / totalEmotions;

        dtos.add(new EmotionStatsDto(emotionLabel, count, totalRatio, avgLevel));

        if (count > maxCount) {
            maxCount = count;
            topEmotion = emotionLabel;
        }
    }

    // 5. 최종 응답 DTO 반환
    String periodLabel = createPeriodLabel(startDate, periodType);
    String aiComment = String.format("이번 %s 동안 가장 자주 느낀 감정은 '%s'이네요!", periodLabel, topEmotion);

    return new EmotionStatsResponseDto(periodType, periodLabel, dtos, topEmotion, aiComment);
}

    /**
     * 통계 기간 라벨을 생성하는 헬퍼 메서드 (예: "2025-09", "2025-W39")
     */
    private String createPeriodLabel(LocalDate startDate, String periodType) {
        if ("MONTH".equals(periodType)) {
            return String.format("%d-%02d", startDate.getYear(), startDate.getMonthValue());
        } else if ("WEEK".equals(periodType)) {
            int weekOfYear = startDate.get(WeekFields.ISO.weekOfWeekBasedYear());
            int weekBasedYear = startDate.get(IsoFields.WEEK_BASED_YEAR);
            return String.format("%d-W%02d", weekBasedYear, weekOfYear);
        }
        return startDate.toString();
    }
    






}
