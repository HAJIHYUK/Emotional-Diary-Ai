package com.jh.emotion.controller;

    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    

    // 감정 분석 요청 및 감정분석 저장 후 결과 반환 
    @PostMapping("/analyze")
    public ResponseEntity<SuccessResponse<EmotionAnalysisResultDto>> analyzeEmotion(@Valid @RequestBody EmotionAnalysisRequestDto request, @RequestParam("userId") Long userId) throws JsonProcessingException {
        log.info("감정 분석 요청: {}", request.getDiaryRecordId());
        EmotionAnalysisResultDto emotionAnalysisResultDto = aiEmotionAnalysisService.analyzeEmotionAndRecommend(userId,request.getDiaryRecordId());
        return ResponseEntity.ok(new SuccessResponse<>(0, "감정 분석 완료 및 저장후 결과 반환", emotionAnalysisResultDto));
    }

    // 감정 분석 결과 조회
    @GetMapping("/result")
    public ResponseEntity<SuccessResponse<EmotionAnalysisResultDto>> getEmotionAnalysisResult(@RequestParam("diaryId") Long diaryId) {
        EmotionAnalysisResultDto emotionAnalysisResultDto = aiEmotionAnalysisService.getEmotionAnalysisResult(diaryId);
        return ResponseEntity.ok(new SuccessResponse<>(0, "감정 분석 결과 조회 완료", emotionAnalysisResultDto));
    }
} 