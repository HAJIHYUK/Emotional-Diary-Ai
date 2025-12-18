package com.jh.emotion.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jh.emotion.dto.SuccessResponse;
import com.jh.emotion.service.NaverSearchService;
import com.jh.emotion.service.UserDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/naver")
@RequiredArgsConstructor
public class NaverController {

    private final NaverSearchService naverSearchService;
    private final UserDataService userDataService;

    @GetMapping("/search")
    public ResponseEntity<SuccessResponse<List<String>>> searchPlaceLinks(
        @RequestParam(name = "query") String query,
        @RequestParam(name = "count") int count,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        String userLocation = userDataService.getUserLocation(userId);
        List<String> links = naverSearchService.searchPlaceLinks(query, count, userLocation);
        return ResponseEntity.ok(new SuccessResponse<>(0, "검색 완료", links));
    }

}
