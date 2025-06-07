package com.jh.emotion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.jh.emotion.dto.DiaryWriteDto;

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
} 