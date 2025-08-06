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
public class EmotionAnalysisResultDto {
    private List<EmotionDto> emotions; // 감정 리스트
    private Long diaryRecordId; // 일기 레코드 ID
}
