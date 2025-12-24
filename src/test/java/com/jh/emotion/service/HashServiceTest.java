package com.jh.emotion.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HashServiceTest {

    @Test
    @DisplayName("동일한 텍스트는 항상 동일한 해시값을 생성해야 함")
    void consistentHashTest() {
        String content = "오늘 날씨가 정말 좋네요.";
        String hash1 = HashService.generateContentHash(content);
        String hash2 = HashService.generateContentHash(content);

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    @DisplayName("글자 내용이 다르면 해시값이 완전히 달라져야 함")
    void differentHashTest() {
        String content1 = "오늘 날씨가 정말 좋네요.";
        String content2 = "오늘 기분이 정말 좋네요.";

        String hash1 = HashService.generateContentHash(content1);
        String hash2 = HashService.generateContentHash(content2);

        assertThat(hash1).isNotEqualTo(hash2);
    }
}
