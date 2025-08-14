package com.jh.emotion.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jh.emotion.dto.SuccessResponse;
import com.jh.emotion.dto.UserClickEventDto;
import com.jh.emotion.service.ClickEventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/user-click-event")
@RequiredArgsConstructor
public class UserClickEventController {

    private final ClickEventService clickEventService;

    @PostMapping("/save")
    public ResponseEntity<SuccessResponse<Void>> saveUserClickEvent(@RequestBody UserClickEventDto userClickEventDto) {
        clickEventService.saveUserClickEvent(userClickEventDto);
        return ResponseEntity.ok(new SuccessResponse<>(0, "유저 클릭 이벤트 저장 완료", null));
    }
}
