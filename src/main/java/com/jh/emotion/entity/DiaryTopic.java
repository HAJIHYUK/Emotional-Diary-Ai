package com.jh.emotion.entity;

import com.jh.emotion.enums.EmotionCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "diary_topics")
@Getter
@Setter
@NoArgsConstructor
public class DiaryTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "topic_text", nullable = false, length = 500)
    private String topicText; // 추천 주제 텍스트

    @Enumerated(EnumType.STRING)
    @Column(name = "emotion_category", nullable = false)
    private EmotionCategory emotionCategory; // 주제가 속한 감정 카테고리 (JOY, SADNESS, ANGER, ANXIETY)

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // 주제 활성화 여부 (기본값 true)
}