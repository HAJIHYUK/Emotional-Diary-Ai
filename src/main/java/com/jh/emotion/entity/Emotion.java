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
    @JoinColumn(name = "user_id")
    private User user;
   
    private String label; // 감정 라벨(“기쁨”, “슬픔” 등)

    private Long level; // 1, 2, 3 등 강도 수준 (필요시) MAX:10 MIN:0

    @Column(nullable = true, length = 500)
    private String description; // 유저에게 알려줄 감정 설명

    private Double confidence; // 감정 분석 신뢰도
    
    
    
    // // 양방향 관계 추가
    // @OneToMany(mappedBy = "emotion", cascade = CascadeType.ALL)
    // private List<DiaryRecord> diaryRecords = new ArrayList<>();
    
    // @OneToMany(mappedBy = "emotion", cascade = CascadeType.ALL)
    // private List<EmotionCache> caches = new ArrayList<>();

    @OneToOne(mappedBy = "emotion", fetch = FetchType.LAZY)
    private DiaryRecord diaryRecord;
}
