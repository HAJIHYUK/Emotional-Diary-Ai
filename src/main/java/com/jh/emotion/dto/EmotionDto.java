package com.jh.emotion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmotionDto {
    private String label; // 감정 라벨
    private Long level; // 감정 강도
    private String description; // 감정 설명
    private Double confidence; // 감정 분석 신뢰도
    private Double ratio; // 감정 비율(선택)
} 