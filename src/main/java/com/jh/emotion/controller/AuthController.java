package com.jh.emotion.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jh.emotion.service.KakaoAuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoAuthService kakaoAuthService;

    //카카오 로그인
    @PostMapping("/kakao")
    public ResponseEntity<Map<String, Object>> kakaoLogin(@RequestBody Map<String, String> requestBody) {
        String code = requestBody.get("code");
        Map<String, Object> response = kakaoAuthService.login(code);
        
        // JWT와 isNewUser가 담긴 Map을 JSON 형태로 반환
        return ResponseEntity.ok(response);
    }
}