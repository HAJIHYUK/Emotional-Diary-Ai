package com.jh.emotion.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionController {
    
    // 유효성 검증 실패 시 발생 (폼 데이터 검증 오류) @Valid 어노테이션 사용 시 발생
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("유효성 검증 실패: {}", ex.getMessage());
            
        Map<String, String> errors = new HashMap<>();
        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        
        for (FieldError error : fieldErrors) {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        }
        
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    
    // Gemini API 클라이언트 오류
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, String>> handleHttpClientErrorException(HttpClientErrorException ex) {
        log.error("API 클라이언트 오류: {}, 응답: {}", ex.getMessage(), ex.getResponseBodyAsString());
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "API 클라이언트 오류");
        error.put("message", ex.getMessage());
        error.put("status", ex.getStatusCode().toString());
        
        return new ResponseEntity<>(error, ex.getStatusCode());
    }
    
    // Gemini API 서버 오류
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<Map<String, String>> handleHttpServerErrorException(HttpServerErrorException ex) {
        log.error("API 서버 오류: {}, 응답: {}", ex.getMessage(), ex.getResponseBodyAsString());
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "API 서버 오류");
        error.put("message", ex.getMessage());
        error.put("status", ex.getStatusCode().toString());
        
        return new ResponseEntity<>(error, HttpStatus.BAD_GATEWAY);
    }
    
    // JSON 처리 오류
    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<Map<String, String>> handleJsonProcessingException(JsonProcessingException ex) {
        log.error("JSON 처리 오류: {}", ex.getMessage());
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "JSON 처리 오류");
        error.put("message", ex.getMessage());
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    // 비즈니스 로직 오류 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("비즈니스 로직 오류: {}", ex.getMessage());
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "비즈니스 로직 오류");
        error.put("message", ex.getMessage());
        
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    // 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        log.error("예상치 못한 오류 발생: {}", ex.getMessage(), ex);
        
        Map<String, String> error = new HashMap<>();
        error.put("error", "시스템 오류");
        error.put("message", "요청 처리 중 오류가 발생했습니다: " + ex.getMessage());
        
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
