package com.jh.emotion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmotionAnalysisResultDto {

    private String emotionLabel; // 감정 라벨
    private Long emotionLevel; // 감정 레벨
    private String description; // 감정 설명
    private Double confidence; // 감정 분석 신뢰도
    private Long diaryRecordId; // 일기 레코드 ID


}
