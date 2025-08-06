package com.jh.emotion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "emotions")
@Getter
@Setter
@NoArgsConstructor
public class Emotion {//감정 정보

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emotionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_record_id")
    private DiaryRecord diaryRecord; // 여러 감정이 하나의 일기에 속함

    private String label; //  감정 라벨 ["기쁨", "슬픔", "분노", "불안", "놀람", "역겨움", "중립"]
    private Long level;   // 1, 2, 3 등 강도 수준 (필요시) MAX:10 MIN:0
    private Double confidence; // 신뢰도
    private Double ratio; // 감정 비율(선택, 없으면 null)
    @Column(nullable = true, length = 500)
    private String description; // 감정 설명

    // user 필드는 필요에 따라 유지/삭제
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
