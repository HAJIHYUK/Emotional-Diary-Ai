package com.jh.emotion.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRecommendationResponseDto {


        private Long diaryRecord; // 일기 레코드 아이디
        private String typePreference; // 추천 타입 선호도
        private String type; // 추천 타입
        private String title; // 추천 제목
        private String reason; // 추천 이유
        private String link; // 추천 링크
        private String linkType; // 추천 링크 타입
        private String genre;
        private Long recommendationId;





}
