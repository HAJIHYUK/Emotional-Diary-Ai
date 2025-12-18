package com.jh.emotion.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import com.jh.emotion.config.JwtTokenProvider;
import com.jh.emotion.entity.User;
import com.jh.emotion.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KakaoAuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WebClient webClient;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String userInfoUri;

    /**
     * [메인 로그인 메서드]
     * 카카오 로그인 전체 프로세스 담당
     * 프론트엔드로부터 받은 인가 코드(code)를 사용하여 로그인을 처리하고, 최종적으로 JWT 토큰을 발급합니다.
     * 
     * 1. 인가 코드로 카카오 액세스 토큰 요청
     * 2. 액세스 토큰으로 카카오 사용자 정보 조회
     * 3. 카카오 정보를 바탕으로 회원가입 또는 정보 업데이트 (DB 처리)
     * 4. 우리 서버의 JWT 토큰 생성 및 반환
     * 
     * @param code 카카오 인증 서버로부터 받은 인가 코드
     * @return JWT 토큰과 신규 가입 여부가 담긴 Map
     */
    public Map<String, Object> login(String code) {
        log.info("카카오 로그인 프로세스 시작. 인증 코드: {}", code);
        
        String accessToken = getAccessToken(code);
        log.info("액세스 토큰 발급 성공.");
        
        Map<String, Object> userInfo = getUserInfo(accessToken);
        log.info("카카오 사용자 정보 조회 성공: {}", userInfo);

        LoginResult loginResult = registerOrUpdateUser(userInfo);
        User user = loginResult.getUser();
        boolean isNewUser = loginResult.isNewUser();

        log.info("사용자 로그인/회원가입 처리 완료. userId: {}, isNewUser: {}", user.getUserId(), isNewUser);
        
        String jwt = jwtTokenProvider.createToken(user.getUserId());
        log.info("JWT 발급 완료.");

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("isNewUser", isNewUser);
        return response;
    }

    /**
     * [액세스 토큰 요청]
     * 카카오 인증 서버에 인가 코드를 전송하여 액세스 토큰을 받아옵니다.
     * 
     * @param code 인가 코드
     * @return 카카오 액세스 토큰 (String)
     */
    private String getAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);
        formData.add("client_secret", clientSecret);

        Map<String, Object> response = webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        return (String) response.get("access_token");
    }

    /**
     * [사용자 정보 조회]
     * 발급받은 액세스 토큰을 사용하여 카카오 리소스 서버에서 사용자의 프로필 정보(ID, 닉네임, 프로필 사진 등)를 조회합니다.
     * 
     * @param accessToken 카카오 액세스 토큰
     * @return 사용자 정보가 담긴 Map 객체
     */
    private Map<String, Object> getUserInfo(String accessToken) {
        return webClient.get()
                .uri(userInfoUri)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    /**
     * [회원 등록 및 업데이트]
     * 카카오 사용자 정보를 바탕으로 우리 DB에 회원을 등록하거나 정보를 업데이트합니다.
     * 
     * - 이미 존재하는 회원인 경우: 정보를 업데이트하고 기존 회원 객체 반환
     * - 신규 회원인 경우: 새로운 User 객체를 생성하여 DB에 저장(회원가입)하고 반환
     * - 탈퇴한 회원의 경우: 탈퇴 후 30일 경과 여부를 확인하여 재가입 허용 또는 거부 처리
     * 
     * @param userInfo 카카오 사용자 정보 Map
     * @return User 객체와 신규 가입 여부가 담긴 LoginResult 객체
     */
    private LoginResult registerOrUpdateUser(Map<String, Object> userInfo) {
        Long kakaoId = ((Number) userInfo.get("id")).longValue();
        Map<String, Object> properties = (Map<String, Object>) userInfo.get("properties");
        String nickname = (String) properties.get("nickname");
        String profileImage = (String) properties.get("profile_image");

        Optional<User> userOptional = userRepository.findByKakaoId(kakaoId);

        User user;
        boolean isNewUser;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            
            if (user.isDeleted()) {
                if (user.getDeletedAt() != null && user.getDeletedAt().plusDays(30).isAfter(LocalDateTime.now())) {
                    throw new RuntimeException("이미 탈퇴한 회원입니다. 탈퇴 후 30일이 지나야 재가입할 수 있습니다.");
                } else {
                    user.setDeleted(false);
                    user.setDeletedAt(null);
                    log.info("탈퇴 후 30일이 지나 재활성화된 사용자. kakaoId: {}", kakaoId);
                }
            }

            log.info("기존 사용자 로그인. kakaoId: {}", kakaoId);
            user.setNickname(nickname);
            user.setProfileImageUrl(profileImage);
            isNewUser = false;
        } else {
            log.info("신규 사용자 회원가입. kakaoId: {}", kakaoId);
            user = new User();
            user.setKakaoId(kakaoId);
            user.setNickname(nickname);
            user.setProfileImageUrl(profileImage);
            
            user = userRepository.save(user); 
            isNewUser = true;
        }
        
        return new LoginResult(user, isNewUser);
    }

    /**
     * [로그인 결과 DTO]
     * registerOrUpdateUser 메서드의 결과를 반환하기 위한 내부 클래스입니다.
     * 
     * - user: DB 처리 후의 User 엔티티 객체 (userId 포함)
     * - isNewUser: 신규 회원 가입 여부 (true: 신규, false: 기존)
     */
    @Getter
    @AllArgsConstructor
    private static class LoginResult {
        private final User user;
        private final boolean isNewUser;
    }
}