package com.jh.emotion.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DiaryDetailDto { // 일기 상세 조회 DTO
    
    private Long DiaryRecordId;
    private LocalDate entryDate; // 일기 작성 날짜
    private String weather; // 날씨 정보
    private String content; // 일기 내용
    private boolean isDraft; // 임시저장 여부
    private int emotionAnalysisCount; // 감정 분석 시도 횟수
    private String createdAt; // 일기 작성 날짜
    private LocalDateTime updatedAt; // 일기 수정 날짜
    private String emotionLabel; // 감정 라벨
    private Long emotionLevel; // 감정 레벨
    private String emotionDescription; // 감정 설명
    private Double emotionConfidence; // 감정 신뢰도

    // 일기 작성 날짜 포맷 변환 
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt != null ? createdAt.toLocalDate().toString() : null;
    }

}
