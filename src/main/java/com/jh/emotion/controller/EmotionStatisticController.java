package com.jh.emotion.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import org.springframework.security.core.userdetails.UserDetails; 
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

    /**
     * 감정 통계 조회
     * @param userDetails
     * @param startDate
     * @param endDate
     * @param periodType 는 WEEK 또는 MONTH 중 하나
     * @return
     */
    @GetMapping("/statistic")
    public ResponseEntity<SuccessResponse<EmotionStatsResponseDto>> getEmotionStatistic(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("startDate") java.time.LocalDate startDate,
            @RequestParam("endDate") java.time.LocalDate endDate,
            @RequestParam("periodType") String periodType) {
        Long userId = Long.parseLong(userDetails.getUsername());
        EmotionStatsResponseDto response = emotionStatisticService.getEmotionStats(userId, startDate, endDate, periodType);
        return ResponseEntity.ok(new SuccessResponse<>(0, "감정 통계 조회 완료", response));
    }
}