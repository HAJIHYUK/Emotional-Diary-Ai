package com.jh.emotion.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
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
@Table(name = "diary_record")
@Getter
@Setter
@NoArgsConstructor
public class DiaryRecord {//일기 정보 (사용자 일기)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long diaryRecordId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(length = 1000, nullable = false)
    private String content;//일기 내용
    
    @Column(nullable = true)
    private String weather; // 날씨 정보

    @Column(nullable = true)
    private LocalDate entryDate; // 일기 작성 날짜 (사용자 지정) why? 사용자가 원하는 날짜로 일기를 작성할 수 있도록
    
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "emotion_id", unique = true, nullable = true)
    private Emotion emotion; // 감정 정보

    @Column(nullable = false)
    private boolean isDraft = false; // true면 임시저장, false면 최종저장

    @Column(nullable = false)
    private int emotionAnalysisCount = 0; // 감정분석 시도 횟수

    @Column(nullable = true)
    private String aiComment; // AI 코멘트

    @CreationTimestamp
    private LocalDateTime createdAt; // 일기 작성 날짜(글을 쓴 당일)
    
    @UpdateTimestamp
    private LocalDateTime updatedAt; // 일기 수정 날짜(자동 생성)
    
    // // 양방향 관계 추가
    // @OneToMany(mappedBy = "diaryRecord", cascade = CascadeType.ALL)
    // private List<Recommendation> recommendations = new ArrayList<>();
}
