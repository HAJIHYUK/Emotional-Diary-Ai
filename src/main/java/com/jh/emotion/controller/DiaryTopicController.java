package com.jh.emotion.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jh.emotion.dto.SuccessResponse;
import com.jh.emotion.entity.DiaryTopic;
import com.jh.emotion.service.DiaryTopicService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/diary-topics")
@RequiredArgsConstructor
public class DiaryTopicController {

    private final DiaryTopicService diaryTopicService;

    //일기 주제 추천 조회
    @GetMapping
    public ResponseEntity<SuccessResponse<List<DiaryTopic>>> getTopicSuggestions(
            @RequestParam("type") String type) {

        // 서비스 로직을 호출하여 추천 주제 리스트를 가져옴
        List<DiaryTopic> suggestions = diaryTopicService.getTopicSuggestions(type);

        // 조회된 리스트를 표준 성공 응답 DTO에 담아 반환함
        return ResponseEntity.ok(new SuccessResponse<>(0, "일기 주제 추천 조회 완료", suggestions));
    }
}
