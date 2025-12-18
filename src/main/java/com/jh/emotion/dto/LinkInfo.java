package com.jh.emotion.dto;

import com.jh.emotion.enums.LinkType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LinkInfo {

    private String link; // 실제 링크 
    private LinkType linkType; // Ui에게 보여줄 링크 타입

}
