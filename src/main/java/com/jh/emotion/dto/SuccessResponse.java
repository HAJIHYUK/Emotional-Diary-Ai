package com.jh.emotion.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SuccessResponse<T> { // 성공 응답 DTO
    private int status; // 0: 성공, 1 이상: 실패/에러
    private String message;
    private T data; // 필요하면 추가 데이터
} 