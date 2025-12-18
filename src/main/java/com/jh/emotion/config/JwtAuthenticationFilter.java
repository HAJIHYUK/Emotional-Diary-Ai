package com.jh.emotion.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User; // Spring Security의 User 객체
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 JWT를 추출합니다.
        String token = resolveToken(request);

        // 2. 토큰이 존재하고, 유효성 검증에 성공하면 인증 처리를 합니다.
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 3. 토큰에서 사용자 ID(pk)를 추출합니다.
            Long userId = jwtTokenProvider.getUserIdFromToken(token);

            // 4. 추출한 사용자 ID로 '인증 객체'를 생성합니다.
            //    (DB에서 실제 사용자 정보를 조회하지 않고, 토큰에 있는 정보만으로 인증)
            UserDetails userDetails = User.builder()
                    .username(String.valueOf(userId))
                    .password("") // 비밀번호는 사용하지 않으므로 비워둠
                    .roles("USER") // 역할 설정 (필요에 따라 DB에서 가져오도록 확장 가능)
                    .build();

            // 5. 인증 객체를 생성하여 SecurityContext에 저장합니다.
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 6. 다음 필터로 요청과 응답을 전달합니다.
        filterChain.doFilter(request, response);
    }

    /**
     * HttpServletRequest의 헤더에서 'Authorization' 필드를 찾아
     * 'Bearer ' 접두사를 제거하고 순수한 JWT 문자열만 추출합니다.
     * @param request
     * @return JWT 문자열 또는 null
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
