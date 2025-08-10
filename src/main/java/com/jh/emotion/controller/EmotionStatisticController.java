package com.jh.emotion.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jh.emotion.dto.EmotionStatsResponseDto;
import com.jh.emotion.dto.SuccessResponse;
import com.jh.emotion.service.EmotionStatisticService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/emotion")
@RequiredArgsConstructor
public class EmotionStatisticController {

    private final EmotionStatisticService emotionStatisticService;

    // 감정 통계 조회
    @GetMapping("/statistic")
    public ResponseEntity<SuccessResponse<EmotionStatsResponseDto>> getEmotionStatistic(
            @RequestParam("userId") Long userId,
            @RequestParam("startDate") java.time.LocalDate startDate,
            @RequestParam("endDate") java.time.LocalDate endDate,
            @RequestParam("periodType") String periodType) {
        EmotionStatsResponseDto response = emotionStatisticService.getEmotionStats(userId, startDate, endDate, periodType);
        return ResponseEntity.ok(new SuccessResponse<>(0, "감정 통계 조회 완료", response));
    }

}
