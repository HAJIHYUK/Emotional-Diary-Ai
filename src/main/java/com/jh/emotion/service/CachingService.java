package com.jh.emotion.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CachingService { // redis 를 이용한 캐시 서비스 (중복방지 + 캐시 효율적 사용)
    private final StringRedisTemplate redisTemplate;
    private static final long CACHE_TTL = 1L; // 캐시 유효시간(시간)

    /**
     * 캐시에서 결과 조회
     * @param content 일기 내용
     * @return 캐시된 결과(없으면 null)
     */
    public String getCachedResult(String content) {
        // HashService를 이용해 해시(key) 생성
        String hash = HashService.generateContentHash(content);
        return redisTemplate.opsForValue().get(hash);
    }

    /**
     * 캐시에 결과 저장
     * @param content 일기 내용
     * @param result 저장할 결과(JSON 등)
     */
    @Transactional(readOnly = false)
    public void setCachedResult(String content, String result) {
        // HashService를 이용해 해시(key) 생성
        String hash = HashService.generateContentHash(content);
        redisTemplate.opsForValue().set(hash, result, CACHE_TTL, TimeUnit.HOURS);
    }
}
