package com.jh.emotion.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class YoutubeLinkRecommendationDto { // 추천정보로 유튜브 링크 변환 Dto

    private String type;
    private String title;
    private String reason;
    private String link;
    private String typePreference;


}
