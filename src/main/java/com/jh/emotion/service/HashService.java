package com.jh.emotion.service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HashService {

    /**
     * 일기 내용만으로 SHA-256 해시값을 생성
     * 줄바꿈/유니코드 정규화/공백 압축/길이 접두어/버전 
     */
    public static String generateContentHash(String content) {
        // 0) null 안전 처리
        String c = content == null ? "" : content;

        // 1) 정규화
        c = normalizeContent(c);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // 2) 버전 접두사
            md.update("emot-v1".getBytes(StandardCharsets.UTF_8));

            // 3) 길이 접두어
            updateWithLengthPrefix(md, c);

            byte[] bytes = md.digest();
            return toHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("해시 생성 실패", e);
        }
    }

    // 일기 내용 정규화: 줄바꿈 통일, 유니코드 정규화, 공백 압축, 소문자 통일
    private static String normalizeContent(String s) {
        // 0) null 안전 처리
        s = s == null ? "" : s;

        // 1) 유니코드 정규화 (시각적으로 같은 문자를 같은 바이트로)
        s = Normalizer.normalize(s, Normalizer.Form.NFKC);

        // 2) 소문자 통일
        s = s.toLowerCase(Locale.ROOT);

        // 3) 의미 없는 단독 자음/모음 제거 (ex: ㅋㅋ, ㅎㅎ, ㅠㅠ)
        s = s.replaceAll("[ㄱ-ㅎㅏ-ㅣ]+", "");

        // 4) 특수문자 및 기호 제거 (한글, 영어, 숫자, 공백만 남김)
        s = s.replaceAll("[^\\p{IsHangul}\\p{IsAlphabetic}\\p{IsDigit}\\s]", "");

        // 5) 공백 압축 (연속된 공백/탭/줄바꿈을 하나의 공백으로)
        s = s.replaceAll("\\s+", " ").trim();

        return s;
    }

    // 위치 정규화: trim, 유니코드, 소문자 통일
    private static String normalizeLocation(String s) {
        s = s.trim();
        s = Normalizer.normalize(s, Normalizer.Form.NFKC);
        s = s.toLowerCase(Locale.ROOT);
        return s;
    }

    // 길이 접두어로 안전하게 경계 표시
    private static void updateWithLengthPrefix(MessageDigest md, String s) {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        ByteBuffer len = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(b.length);
        md.update(len.array());
        md.update(b);
    }

    // 바이트 배열을 16진수 문자열로 변환
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte x : bytes) sb.append(String.format("%02x", x));
        return sb.toString();
    }
}