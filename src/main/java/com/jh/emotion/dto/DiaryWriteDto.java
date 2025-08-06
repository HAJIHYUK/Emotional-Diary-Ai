package com.jh.emotion.dto;


import java.time.LocalDate;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DiaryWriteDto { // 일기 작성 DTO

    private Long userId; // 유저 아이디

    @Size(max = 1000, message = "일기는 1000자 이내로 작성해주세요.")
    private String content;//일기 내용

    private String weather; // 날씨 정보
    
    private LocalDate entryDate; // 일기 작성 날짜 (사용자 지정) why? 사용자가 원하는 날짜로 일기를 작성할 수 있도록
    

}
