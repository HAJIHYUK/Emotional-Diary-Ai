package com.jh.emotion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/diary")
    public String diary() {
        return "diary_write";
    }

    @GetMapping("/user-preference")
    public String userPreference() {
        return "user_preference_input";
    }

    @GetMapping("/naver-search")
    public String naverSearch() {
        return "naver_search";
    }

    @GetMapping("/kakaomap")
    public String kakaomap() {
        return "kakaomap";
    }

    @GetMapping("/emotion-stats")
    public String emotionStats() {
        return "emotion_stats_test";
    }
} 