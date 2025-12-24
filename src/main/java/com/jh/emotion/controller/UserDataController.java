package com.jh.emotion.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jh.emotion.dto.SuccessResponse;
import com.jh.emotion.service.UserDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user-data")
@RequiredArgsConstructor
public class UserDataController {

    private final UserDataService userDataService;

    @PostMapping("/savelocation")
    public ResponseEntity<SuccessResponse<Void>> saveUserLocation(
            @RequestParam("location") String location,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userDataService.saveUserLocation(userId, location);
        return ResponseEntity.ok(new SuccessResponse<>(0, "유저 위치 저장 완료", null));
    }

    //유저 탈퇴 (Soft Delete)
    @PostMapping("/deactivate")
    public ResponseEntity<SuccessResponse<Void>> deactivateUser(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userDataService.deactivateUser(userId);
        return ResponseEntity.ok(new SuccessResponse<>(0, "유저 탈퇴 처리 완료", null));
    }
}
