package com.jh.emotion.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserPreferenceResponseDto {

    private Long userPreferenceId;
    private String category;
    private String genre;
    private String type;
    private Integer useCount;


}
