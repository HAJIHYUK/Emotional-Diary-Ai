package com.jh.emotion.repository;

import com.jh.emotion.entity.DiaryTopic;
import com.jh.emotion.enums.EmotionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryTopicRepository extends JpaRepository<DiaryTopic, Long> {

    /**
     * 활성화된 주제 중 특정 감정 카테고리에 맞는 모든 주제 찾기
     * @param category 조회할 감정 카테고리
     * @return 주제 리스트
     */
    List<DiaryTopic> findByEmotionCategoryAndIsActiveTrue(EmotionCategory category);

    /**
     * 모든 활성화된 주제 찾기
     * @return 주제 리스트
     */
    List<DiaryTopic> findByIsActiveTrue();
}