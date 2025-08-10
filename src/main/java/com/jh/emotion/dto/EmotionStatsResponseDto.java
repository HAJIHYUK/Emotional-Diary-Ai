package com.jh.emotion.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmotionStatsResponseDto {

    private String periodType; // "WEEK" or "MONTH" (주/월 표시)
    private String periodLabel; // "2024-06", "2024-W23" (정확한 주/월 표시)
    private List<EmotionStatsDto> stats; // 감정별 통계 리스트
    private String topEmotion; // 감정 최빈값 (ex.기쁨)
    private String aiComment; // AI 코멘트(조언/칭찬/위로)
    
}
