package com.jh.emotion.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; // import 추가
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jh.emotion.dto.DiaryDetailDto;
import com.jh.emotion.dto.DiaryListDto;
import com.jh.emotion.dto.DiaryWriteDto;
import com.jh.emotion.dto.SuccessResponse;
import com.jh.emotion.dto.UserRecommendationResponseDto;
import com.jh.emotion.entity.DiaryRecord;
import com.jh.emotion.service.RecommendationService; // RecommendationService import 추가
import com.jh.emotion.service.DiaryService;
import com.jh.emotion.service.RecommendationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final RecommendationService recommendationService; // RecommendationService 주입

    //일기 작성
    @PostMapping("/write")
    public ResponseEntity<SuccessResponse<Map<String, Object>>> writeDiary(@RequestBody DiaryWriteDto dto) {
        DiaryRecord record = diaryService.createDiaryRecord(dto);
        Map<String, Object> data = new HashMap<>();
        data.put("recordId", record.getDiaryRecordId()); //일기 번호
        return ResponseEntity.ok(new SuccessResponse<>(0, "일기 생성 완료", data));
    }

    //일기 목록 조회
    @GetMapping("/list")
    public ResponseEntity<SuccessResponse<List<DiaryListDto>>> getDiaryList(@RequestParam("userId") Long userId) {
        List<DiaryListDto> list = diaryService.getDiaryList(userId);
        return ResponseEntity.ok(new SuccessResponse<>(0, "일기 목록 조회 완료", list));
    }

    //일기 상세 조회
    @GetMapping("/detail")
    public ResponseEntity<SuccessResponse<DiaryDetailDto>> getDiaryDetail(@RequestParam("diaryId") Long diaryId) {
        DiaryDetailDto detail = diaryService.getDiaryDetail(diaryId);
        return ResponseEntity.ok(new SuccessResponse<>(0, "일기 상세 조회 완료", detail));
    }

    //일기 상세 수정
    @PostMapping("/update")
    public ResponseEntity<SuccessResponse<Void>> updateDiary(@RequestParam("diaryId") Long diaryId, @RequestBody DiaryWriteDto dto) { // @RequestBody 추가
        diaryService.updateDiaryRecord(diaryId, dto);
        return ResponseEntity.ok(new SuccessResponse<>(0, "수정 완료", null));
    }

    //일기 삭제
    @PostMapping("/delete")
    public ResponseEntity<SuccessResponse<Void>> deleteDiary(@RequestParam("diaryId") Long diaryId) {
        diaryService.deleteDiaryRecord(diaryId);
        return ResponseEntity.ok(new SuccessResponse<>(0, "삭제 완료", null));
    }

    //일기에 있는 추천 정보 조회
    @GetMapping("/recommendations")
    public ResponseEntity<SuccessResponse<List<UserRecommendationResponseDto>>> getRecommendations(@RequestParam("diaryId") Long diaryId) {
        List<UserRecommendationResponseDto> recommendations = recommendationService.getRecommendations(diaryId);
        return ResponseEntity.ok(new SuccessResponse<>(0, "추천 정보 조회 완료", recommendations));
    }

    
    
    
}