package com.jh.emotion.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.jh.emotion.enums.PreferenceCategory;
import com.jh.emotion.enums.PreferenceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "user_preferences", // 유저 선호도 정보 , 유저 아이디, 카테고리, 아이템 이름 중복 불가
    // why? 새로운 유저 취향발견 DISCOVERED 시 기존에 있던거와 겹치면 안됨
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category", "item_name"})
)
@Getter
@Setter
@NoArgsConstructor
public class UserPreference { // 유저 선호도 정보
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userPreferenceId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PreferenceCategory category; // 대분류/ MUSIC, MOVIE, CAFE 등 
    
    @Column(nullable = false)
    private String genre; // 장르/소분류 (ex. ACTION, TERRACE, DESSERT)
    
    @Enumerated(EnumType.STRING)
    private PreferenceType type; // INITIAL(초기입력), DISCOVERED(발견됨)
    
    private Integer useCount; // 사용 횟수
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime lastUsedAt;
}


