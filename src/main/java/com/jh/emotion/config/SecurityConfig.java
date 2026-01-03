package com.jh.emotion.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
//으 어지러워 설정 
@Configuration
@RequiredArgsConstructor // JwtTokenProvider 주입을 위해 추가 
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider; // JWT 프로바이더 주입

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // [정적 리소스 허용] 리액트 빌드 파일들은 인증 없이 접근 가능해야 함
                .requestMatchers("/", "/index.html", "/assets/**", "/*.ico", "/*.js", "/*.css", "/vite.svg").permitAll()
                // [API 인증 예외] 로그인 관련 API 허용
                .requestMatchers("/api/auth/**").permitAll()
                // [API 인증 필수] 나머지 API는 토큰 필요
                .requestMatchers("/api/**").authenticated()
                // 그 외 모든 요청(프론트엔드 라우팅 등)은 허용
                .anyRequest().permitAll()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // [CORS 수정] 배포 도메인 추가
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173", 
            "http://127.0.0.1:5173", 
            "https://diaryai.kro.kr", // 배포 도메인
            "http://diaryai.kro.kr"   // 혹시 모를 http 접속 대비
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
