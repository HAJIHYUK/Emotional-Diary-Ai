package com.jh.emotion.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jh.emotion.dto.SuccessResponse;
import com.jh.emotion.dto.UserPreferenceInitialRequestDto;
import com.jh.emotion.dto.UserPreferenceResponseDto;
import com.jh.emotion.service.UserPreferenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user-preference")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @PostMapping("/save")
    public ResponseEntity<SuccessResponse<Void>> saveUserPreference(@RequestBody List<UserPreferenceInitialRequestDto> userPreferenceInitialDto, @RequestParam("userId") Long userId) {
        userPreferenceService.saveUserPreference(userPreferenceInitialDto,userId);
        return ResponseEntity.ok(new SuccessResponse<>(0, "유저 선호도 저장 완료", null));
    }

    @GetMapping("/list")
    public ResponseEntity<SuccessResponse<List<UserPreferenceResponseDto>>> getUserPreference(@RequestParam("userId") Long userId) {
        List<UserPreferenceResponseDto> preferences = userPreferenceService.getUserPreference(userId);
        return ResponseEntity.ok(new SuccessResponse<>(0, "유저 선호도 조회 완료", preferences));
    }

}
