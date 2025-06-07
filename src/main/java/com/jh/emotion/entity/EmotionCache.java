package com.jh.emotion.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "emotion_cache", indexes = {
    @Index(name = "idx_content_hash", columnList = "content_hash")
})
@Getter
@Setter
@NoArgsConstructor
public class EmotionCache {//감정 캐시 정보

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cacheId;
    
    @Column(length = 64, nullable = false, name = "content_hash")
    private String contentHash;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emotion_id")
    private Emotion emotion;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    private LocalDateTime expiresAt;
}
