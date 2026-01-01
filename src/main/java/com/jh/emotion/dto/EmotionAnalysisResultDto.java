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
    private List<String> keywords; // [추가] 일기 핵심 키워드 (AI 추출)
    
    // 기존 생성자 호환을 위한 생성자 추가, 키워드 추가 해서 그럼 
    public EmotionAnalysisResultDto(List<EmotionDto> emotions, Long diaryRecordId) {
        this.emotions = emotions;
        this.diaryRecordId = diaryRecordId;
    }
}
