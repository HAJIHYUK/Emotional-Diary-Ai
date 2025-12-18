package com.jh.emotion.service;

import com.jh.emotion.entity.DiaryTopic;
import com.jh.emotion.enums.EmotionCategory;
import com.jh.emotion.repository.DiaryTopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiaryTopicService {

    private final DiaryTopicRepository diaryTopicRepository;
    private static final int RECOMMENDATION_LIMIT = 3; // 추천 개수 3개로 제한함

    public List<DiaryTopic> getTopicSuggestions(String type) {
        List<DiaryTopic> topics;

        if ("random".equalsIgnoreCase(type)) {
            // type이 "random"일 경우, 모든 활성화 주제 찾기
            topics = diaryTopicRepository.findByIsActiveTrue();
        } else {
            // 그 외의 경우, 문자열을 EmotionCategory Enum 타입으로 변환하여 주제 찾기
            try {
                EmotionCategory emotionCategory = EmotionCategory.valueOf(type.toUpperCase());
                topics = diaryTopicRepository.findByEmotionCategoryAndIsActiveTrue(emotionCategory);
            } catch (IllegalArgumentException e) {
                // 유효하지 않은 감정 카테고리 문자열이 들어온 경우, 빈 리스트를 반환하거나 예외 처리를 할 수 있음
                return Collections.emptyList();
            }
        }

        // 주제 리스트를 무작위로 섞음
        Collections.shuffle(topics);

        // 섞인 리스트에서 추천 개수만큼 잘라내어 새로운 리스트로 반환함
        int toIndex = Math.min(topics.size(), RECOMMENDATION_LIMIT);
        return new ArrayList<>(topics.subList(0, toIndex));
    }
}
