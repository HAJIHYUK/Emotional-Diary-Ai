package com.jh.emotion.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaryListDto { // 일기 목록 조회 DTO
    private Long diaryRecordId; // 일기 고유 아이디
    private LocalDate entryDate; // 일기 작성 날짜
    private String weather; // 날씨 정보
    private List<EmotionDto> emotions; // 감정 리스트
    private String content; // 내용
    private boolean isDraft; // 임시저장 여부
    private LocalDateTime createdAt; // 일기 작성 날짜 (시간 포함)
}