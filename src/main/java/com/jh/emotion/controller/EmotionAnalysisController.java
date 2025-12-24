package com.jh.emotion.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jh.emotion.dto.EmotionAnalysisRequestDto;
import com.jh.emotion.dto.EmotionAnalysisResultDto;
import com.jh.emotion.dto.SuccessResponse;
import com.jh.emotion.service.AiEmotionAnalysisService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/emotion")
@RequiredArgsConstructor
public class EmotionAnalysisController {

    private final AiEmotionAnalysisService aiEmotionAnalysisService;
    
    // 감정 분석 요청 (비동기 처리)
    // [리팩토링] 비동기 요청 접수 후 즉시 응답 반환
    @PostMapping("/analyze")
    public ResponseEntity<SuccessResponse<Void>> analyzeEmotion(
            @Valid @RequestBody EmotionAnalysisRequestDto request, 
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        log.info("감정 분석 요청(Async): diaryId={}, userId={}", request.getDiaryRecordId(), userId);
        
        // 비동기 서비스 호출 (Non-blocking)
        aiEmotionAnalysisService.analyzeEmotionAndRecommend(userId, request.getDiaryRecordId());
        
        return ResponseEntity.ok(new SuccessResponse<>(0, "감정 분석 요청이 접수되었습니다.", null));
    }

    // 감정 분석 결과 조회
    @GetMapping("/result")
    public ResponseEntity<SuccessResponse<EmotionAnalysisResultDto>> getEmotionAnalysisResult(@RequestParam("diaryId") Long diaryId) {
        EmotionAnalysisResultDto emotionAnalysisResultDto = aiEmotionAnalysisService.getEmotionAnalysisResult(diaryId);
        return ResponseEntity.ok(new SuccessResponse<>(0, "감정 분석 결과 조회 완료", emotionAnalysisResultDto));
    }
}