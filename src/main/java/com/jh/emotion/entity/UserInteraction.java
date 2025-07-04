// package com.jh.emotion.entity;

// import java.time.LocalDateTime;

// import org.hibernate.annotations.CreationTimestamp;

// import jakarta.persistence.Column;
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
// @Table(name = "user_interactions")
// @Getter
// @Setter
// @NoArgsConstructor
// public class UserInteraction {

//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long interactionId;
    
//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name = "user_id")
//     private User user;
    
//     @ManyToOne(fetch = FetchType.LAZY)
//     @JoinColumn(name = "recommendation_id")
//     private Recommendation recommendation;
    
//     @Column(nullable = false)
//     private String action;
    
//     @CreationTimestamp
//     private LocalDateTime createdAt;
// }
