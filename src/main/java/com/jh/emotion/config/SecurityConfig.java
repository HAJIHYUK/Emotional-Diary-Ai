package com.jh.emotion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // 모든 요청에 대해 로그인 없이 접근 가능
            );

        return http.build(); // 테스트 수정
    }
}
