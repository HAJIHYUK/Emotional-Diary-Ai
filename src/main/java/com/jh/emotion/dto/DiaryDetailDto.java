package com.jh.emotion.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DiaryDetailDto { // 일기 상세 조회 DTO
    private Long diaryRecordId;
    private LocalDate entryDate; // 일기 작성 날짜
    private String weather; // 날씨 정보
    private String content; // 일기 내용
    private boolean isDraft; // 임시저장 여부
    private int emotionAnalysisCount; // 감정분석 시도 횟수
    private String createdAt; // 일기 작성 날짜
    private LocalDateTime updatedAt; // 일기 수정 날짜
    private List<EmotionDto> emotions; // 감정 리스트

    // 일기 작성 날짜 포맷 변환
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt != null ? createdAt.toLocalDate().toString() : null;
    }
}
