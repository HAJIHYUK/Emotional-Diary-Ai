package com.jh.emotion.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.emotion.dto.UserClickEventDto;
import com.jh.emotion.entity.Recommendation;
import com.jh.emotion.entity.User;
import com.jh.emotion.entity.UserClickEvent;
import com.jh.emotion.enums.PreferenceCategory;
import com.jh.emotion.repository.RecommendationRepository;
import com.jh.emotion.repository.UserClickEventRepository;
import com.jh.emotion.repository.UserPreferenceRepository;
import com.jh.emotion.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClickEventService {

    private final UserClickEventRepository userClickEventRepository;
    private final UserRepository userRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    //userId를 파라미터로 직접 받도록 변경
    @Transactional(readOnly = false)
    public void saveUserClickEvent(UserClickEventDto userClickEventDto, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Long recId;
        if (userClickEventDto.getRecommendationId() != null) {
            recId = userClickEventDto.getRecommendationId();
        } else {
            throw new EntityNotFoundException("RecommendationId is null");
        }

        //오늘 이미 클릭한 추천인지 확인 (중복 방지)를 위한 시간 설정
        LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = java.time.LocalDate.now().atTime(java.time.LocalTime.MAX);
        
        //오늘 이미 클릭한 추천인지 확인 (중복 방지)
        if (userClickEventRepository.existsByUser_UserIdAndRecommendation_RecommendationIdAndCreatedAtBetween(userId, recId, startOfDay, endOfDay)) {
            log.info("[ClickEvent] 이미 오늘 클릭한 추천입니다. 저장을 건너뜁니다. (userId={}, recId={})", userId, recId);
            return;
        }

        Recommendation recommendation = recommendationRepository.findById(recId)
            .orElseThrow(() -> new EntityNotFoundException("Recommendation not found"));
            
        UserClickEvent userClickEvent = new UserClickEvent();
        userClickEvent.setUser(user);
        userClickEvent.setType(userClickEventDto.getType());
        userClickEvent.setTitle(userClickEventDto.getTitle());
        userClickEvent.setGenre(userClickEventDto.getGenre());
        userClickEvent.setRecommendation(recommendation);
        userClickEventRepository.save(userClickEvent);
        log.info("[ClickEvent] 저장 완료: {}", userClickEvent);

        //취향 사용 횟수 업데이트 로직 호출 시 userId 전달
        updateUserPreferenceUsage(userClickEventDto, userId);
    }


    private void updateUserPreferenceUsage(UserClickEventDto userClickEventDto, Long userId) {
        if (userClickEventDto.getGenre() == null || userClickEventDto.getGenre().isEmpty()) {
            return; // 취향에 장르가 없으면 업데이트 X 
        }

        try {
            PreferenceCategory category = PreferenceCategory.valueOf(userClickEventDto.getType());
            
            userPreferenceRepository.findByUser_UserIdAndCategoryAndGenreAndIsActiveTrue(
                userId, // DTO 대신 파라미터로 받은 userId 사용
                category,
                userClickEventDto.getGenre()
            ).ifPresent(preference -> {
                preference.setUseCount(preference.getUseCount() + 1);
                log.info("[Preference Usage] 취향 사용 업데이트: userId={}, category={}, genre={}, newCount={}",
                    preference.getUser().getUserId(), preference.getCategory(), preference.getGenre(), preference.getUseCount());
            });

        } catch (IllegalArgumentException e) {
            log.warn("[Preference Usage] 유효하지 않은 카테고리({})입니다. 취향 사용 업데이트를 건너뜁니다.", userClickEventDto.getType());
        }
    }
}