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

    private String link;
    private LinkType linkType;

}
