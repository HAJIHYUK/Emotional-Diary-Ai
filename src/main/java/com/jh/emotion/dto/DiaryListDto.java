package com.jh.emotion.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DiaryListDto { // 일기 목록 조회 DTO
    private Long DiaryRecordId; // 일기 고유 아이디
    private LocalDate entryDate; // 일기 작성 날짜
    private String weather; // 날씨 정보
    private String emotionLabel; // 감정 라벨
    private boolean isDraft; // 임시저장 여부
    private String createdAt; // 일기 작성 날짜

    // 일기 작성 날짜 포맷 변환 
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt != null ? createdAt.toLocalDate().toString() : null;
    }
}
