package com.jh.emotion.dto;

import java.util.List;

import com.jh.emotion.enums.PreferenceCategory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserPreferenceInitialRequestDto {
    private PreferenceCategory category; // 선호도 카테고리
    private List<String> itemNames; // 선호도 아이템 이름
}