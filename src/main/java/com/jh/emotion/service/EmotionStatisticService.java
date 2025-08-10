package com.jh.emotion.service;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.emotion.dto.EmotionStatsDto;
import com.jh.emotion.dto.EmotionStatsResponseDto;
import com.jh.emotion.repository.EmotionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmotionStatisticService {

    private final EmotionRepository emotionRepository;

    // 감정통계 월별/주별 조회 (ex. 2025-06 월 조회 , 2025-07월 조회 ) , 변수(userId, startDate(날짜시작일), endDate(날짜종료일), periodType(MONTH, WEEK))
    public EmotionStatsResponseDto getEmotionStats(Long userId, LocalDate startDate, LocalDate endDate, String periodType) {
        Long maxCount = 0L; // 최빈값 찾기 위한 변수 (감정 갯수 최대값 저장)
        String topEmotion = ""; // 최빈값 찾기 위한 변수 (가장 많이 나온 감정 저장)

        List<Object[]> emotionStats = emotionRepository.getEmotionStats(userId, startDate, endDate.plusDays(1)); // LocalDate로 쿼리 plusDays(1) 추가하여 날짜 경계값 문제 해결
        long total = emotionStats.stream().mapToLong(arr -> (Long) arr[1]).sum(); // 총 감정 갯수 계산
        List<EmotionStatsDto> dtos = new ArrayList<>(); // 감정 통계 데이터 저장

        for (Object[] arr : emotionStats) { // 감정 통계 데이터 저장
            String emotionLabel = (String) arr[0];
            long count = (Long) arr[1];
            double ratio = (double) count / total;
            double avgLevel = (Double) arr[2];
            dtos.add(new EmotionStatsDto(emotionLabel, count, ratio, avgLevel));
        }
        // 최빈값 찾기
        for (EmotionStatsDto dto : dtos) {
            if (dto.getCount() > maxCount) {
                maxCount = dto.getCount();
                topEmotion = dto.getEmotionLabel();
            }
        }

        String periodLabel = "";
        if ("MONTH".equals(periodType)) { // 월별 조회
            periodLabel = String.format("%d-%02d", startDate.getYear(), startDate.getMonthValue());
        } else if ("WEEK".equals(periodType)) { // 주별 조회
            int weekOfYear = startDate.get(WeekFields.ISO.weekOfWeekBasedYear());// 주차 조회(ISO 표준 ex.W32)
            int weekBasedYear = startDate.get(IsoFields.WEEK_BASED_YEAR);// 주차 조회 (연도)
            periodLabel = String.format("%d-W%02d", weekBasedYear, weekOfYear);
        }

        EmotionStatsResponseDto responseDto = new EmotionStatsResponseDto();
        responseDto.setPeriodType(periodType);
        responseDto.setPeriodLabel(periodLabel);
        responseDto.setStats(dtos);
        responseDto.setTopEmotion(topEmotion);
        // responseDto.setAiComment(...); // AI 멘트 추가 추후 하기 
        

        return responseDto;
    }
    






}
