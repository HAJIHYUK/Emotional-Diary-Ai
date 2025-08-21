package com.jh.emotion.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.emotion.dto.UserClickEventDto;
import com.jh.emotion.entity.Recommendation;
import com.jh.emotion.entity.User;
import com.jh.emotion.entity.UserClickEvent;
import com.jh.emotion.repository.RecommendationRepository;
import com.jh.emotion.repository.UserClickEventRepository;
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

    // 클릭 이벤트 저장 ()
    @Transactional(readOnly = false)
    public void saveUserClickEvent(UserClickEventDto userClickEventDto) {
        log.info("[ClickEvent] 저장 요청: {}", userClickEventDto);
        User user = userRepository.findById(userClickEventDto.getUserId())
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Long recId;
        if (userClickEventDto.getRecommendationId() != null) {
            if (userClickEventDto.getRecommendationId() instanceof Number) {
                recId = ((Number)userClickEventDto.getRecommendationId()).longValue();
            } else {
                recId = Long.parseLong(userClickEventDto.getRecommendationId().toString());
            }
        } else {
            throw new EntityNotFoundException("RecommendationId is null");
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
    }

    









}
