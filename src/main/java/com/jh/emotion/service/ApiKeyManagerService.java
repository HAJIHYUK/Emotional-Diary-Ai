package com.jh.emotion.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.jh.emotion.config.YouTubeConfig;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiKeyManagerService {

    private final YouTubeConfig youTubeConfig;

    private final Map<String, List<String>> apiKeys = new ConcurrentHashMap<>(); // HashMap 대시 멀티 스레드 환경을 대비한 ConcurrentHashMap 사용 
    private final Map<String, AtomicInteger> keyIndexes = new ConcurrentHashMap<>(); //Integer 대신 멀티 스레드 환경을 대비한 AtomicInteger 사용

    @PostConstruct
    public void init() {
        // YouTubeConfig에서 키를 가져와 초기화
        List<String> youtubeKeys = youTubeConfig.getKeys();
        if (youtubeKeys != null && !youtubeKeys.isEmpty()) {
            apiKeys.put("YOUTUBE", youtubeKeys);
            keyIndexes.put("YOUTUBE", new AtomicInteger(0));
        }
    }

    public String getApiKey(String serviceName) {
        List<String> keys = apiKeys.get(serviceName);
        if (keys == null || keys.isEmpty()) {
            throw new IllegalStateException(serviceName + "의 API 키가 설정되지 않았습니다.");
        }

        AtomicInteger index = keyIndexes.get(serviceName);
        int nextIndex = index.getAndIncrement() % keys.size();
        return keys.get(nextIndex);
    }
}