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
@Table(name = "userClickEvent")
@Getter
@Setter
@NoArgsConstructor
public class UserClickEvent {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userClickEventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String type;

    @Column(nullable = true) // 아직 사용안함 제대로 , 확장성을 위해 일단 만들어놓음 나중에 할지 말지 결정할듯 
    private String itemName;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY) // 추천 정보
    @JoinColumn(name = "recommendation_id", nullable = true) // 추천 클릭이 있으면 값이 있고 추천 없이 클릭이 있으면 값이 NULL
    private Recommendation recommendation;
}
