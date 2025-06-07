// package com.jh.emotion.entity;

// import java.time.LocalDate;
// import java.time.LocalDateTime;

// import org.hibernate.annotations.CreationTimestamp;

// import jakarta.persistence.Entity;
// import jakarta.persistence.FetchType;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.Table;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
// import lombok.Setter;

// @Entity
// @Table(name = "emotion_statistics")
// @Getter
// @Setter
// @NoArgsConstructor
// public class EmotionStatistic {//감정 통계 정보

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long statisticId;
    
//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name = "user_id")
//     private User user;
    
//     private Integer count;
    
//     private LocalDate periodStart;
    
//     private LocalDate periodEnd;
    
//     @CreationTimestamp
//     private LocalDateTime createdAt;
// }
