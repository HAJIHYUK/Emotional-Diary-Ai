package com.jh.emotion.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "diary_record",
    indexes = {
        @Index(name = "idx_content_hash", columnList = "contentHash")
    } // 해시값 인덱스 추가
)
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
    
    // 기존 Emotion 필드 삭제
    // @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    // @JoinColumn(name = "emotion_id", unique = true, nullable = true)
    // private Emotion emotion;

    // 복합 감정 구조로 변경
    @OneToMany(mappedBy = "diaryRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Emotion> emotions = new ArrayList<>();

    @Column(nullable = false)
    private boolean isDraft = false; // true면 임시저장, false면 최종저장

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false; // 삭제 여부 true면 삭제, false면 삭제 안됨

    @Column(nullable = false)
    private int emotionAnalysisCount = 0; // 감정분석 시도 횟수

    @Column(nullable = true)
    private String aiComment; // AI 코멘트

    @Column(length = 64)
    private String contentHash; // (일기+사용자위치(지역)) 해시값 저장

    @CreationTimestamp
    private LocalDateTime createdAt; // 일기 작성 날짜(글을 쓴 당일)
    
    @UpdateTimestamp
    private LocalDateTime updatedAt; // 일기 수정 날짜(자동 생성)
    
    // // 양방향 관계 추가
    // @OneToMany(mappedBy = "diaryRecord", cascade = CascadeType.ALL)
    // private List<Recommendation> recommendations = new ArrayList<>();
}