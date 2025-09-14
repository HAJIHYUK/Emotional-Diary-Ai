package com.jh.emotion.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

// LocalDate 타입을 직렬화 하지 않도록 설정

@Configuration
public class JacksonConfig {
    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        // 날짜/시간을 배열이 아닌 ISO-8601 표준 문자열로 직렬화하도록 설정
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
} 