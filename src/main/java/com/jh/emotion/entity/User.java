package com.jh.emotion.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {//유저 정보

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = true)
    private Long kakaoId;
    
    @Column(unique = true, nullable = true)
    private String email;
    
    @Column(nullable = true)
    private String passwordHash;

    @Column(nullable = false)
    private String nickname;
    
    @Column(nullable = true)
    private String location;

    @Column(nullable = true) //카카오 프로필 이미지 URL
    private String profileImageUrl;
    
    @Column(nullable = false)
    private boolean isPremium;
    
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false; // [수정] 기본값 false 설정
    
    @Column(name = "deleted_at", nullable = true) // [추가] 탈퇴 시각 기록
    private LocalDateTime deletedAt;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    
    // // 양방향 관계 추가
    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    // private List<DiaryRecord> diaryRecords = new ArrayList<>();
    
    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    // private List<Recommendation> recommendations = new ArrayList<>();
    
    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    // private List<UserInteraction> interactions = new ArrayList<>();
    
    // @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    // private List<EmotionStatistic> statistics = new ArrayList<>();
}
