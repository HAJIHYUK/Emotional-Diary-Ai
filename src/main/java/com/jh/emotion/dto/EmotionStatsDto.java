package com.jh.emotion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmotionStatsDto {

    private String emotionLabel; // "기쁨", "슬픔" 등
    private Long count; // 감정 개수
    private Double ratio; // 전체 대비 비율
    private Double avgLevel; // 평균 감정 레벨

}
