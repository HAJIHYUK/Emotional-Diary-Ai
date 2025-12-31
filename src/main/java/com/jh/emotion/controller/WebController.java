package com.jh.emotion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 프론트엔드(React) 라우팅을 위한 컨트롤러
 * - "/api"로 시작하지 않는 모든 주소를 index.html로 포워딩
 */
@Controller
public class WebController {

    @GetMapping(value = {
        "/", 
        "/login", 
        "/onboarding", 
        "/diary/**", 
        "/write", 
        "/edit/**", 
        "/settings",
        "/auth/**"
    })
    public String forward() {
        // index.html로 내부 전달(forward)하여 리액트 라우터가 처리하게 함
        return "forward:/index.html";
    }
}
