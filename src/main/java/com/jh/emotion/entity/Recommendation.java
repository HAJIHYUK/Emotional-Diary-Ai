package com.jh.emotion.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recommendations")
@Getter
@Setter
@NoArgsConstructor
public class Recommendation {//추천 정보

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recommendationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_record_id")
    private DiaryRecord diaryRecord;
    
    private String typePreference; // matching_preferences or non_matching_preferences , 나중에 이뮨으로 바꾸는게 좋을듯?

    @Column(nullable = false)
    private String type; // 대분류 추천 타입 (MOVIE, MUSIC, CAFE 등)

    @Column(nullable = false)
    private String genre; // 소분류 추천 타입 (ex. ACTION, TERRACE, DESSERT)
    
    private String title;// 추천 제목

    private String reason;// 추천 이유

    private String link;// 추천 링크
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // // 양방향 관계 추가
    // @OneToMany(mappedBy = "recommendation", cascade = CascadeType.ALL)
    // private List<UserInteraction> interactions = new ArrayList<>();
}
