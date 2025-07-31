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

        User user = userRepository.findById(userClickEventDto.getUserId())
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Recommendation recommendation = recommendationRepository.findById(userClickEventDto.getRecommendationId())
        .orElseThrow(() -> new EntityNotFoundException("Recommendation not found"));
        
        UserClickEvent userClickEvent = new UserClickEvent();
        userClickEvent.setUser(user);
        userClickEvent.setType(userClickEventDto.getType());
        userClickEvent.setItemName(null); // 아직 사용안함 제대로 , 확장성을 위해 일단 만들어놓음 나중에 할지 말지 결정할듯 
        userClickEvent.setRecommendation(recommendation);
        userClickEventRepository.save(userClickEvent);
    }
}
