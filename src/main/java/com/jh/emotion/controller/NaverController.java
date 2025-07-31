package com.jh.emotion.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jh.emotion.dto.SuccessResponse;
import com.jh.emotion.service.NaverSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/naver")
@RequiredArgsConstructor
public class NaverController {

    private final NaverSearchService naverSearchService;

    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<List<String>>> searchPlaceLinks(
        @RequestParam(name = "query") String query,
        @RequestParam(name = "count") int count) {
        List<String> links = naverSearchService.searchPlaceLinks(query, count,"행궁동");
        return ResponseEntity.ok(new SuccessResponse<>(0, "검색 완료", links));
    }

}
