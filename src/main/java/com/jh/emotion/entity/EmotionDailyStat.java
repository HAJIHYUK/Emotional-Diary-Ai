package com.jh.emotion.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Getter
@Setter
@NoArgsConstructor
@Table(name = "emotion_daily_stat", uniqueConstraints = {
    // 한 명의 유저가 같은 날짜에 동일한 감정 라벨을 중복으로 갖지 않도록 보장합니다.
    @UniqueConstraint(columnNames = {"user_id", "stat_date", "emotion_label"})
})
public class EmotionDailyStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emotion_daily_stat_id")
    private Long id; // 통계 데이터의 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 어떤 사용자의 통계인지 (User 엔티티와 연결)

    @Column(name = "stat_date", nullable = false)
    private LocalDate date; // 일기 작성 시 사용자가 지정한 날짜 (entryDate 기준)

    @Column(name = "emotion_label", nullable = false)
    private String emotionLabel; // 감정의 종류 (예: "기쁨", "슬픔")

    @Column(name = "emotion_count", nullable = false)
    private int emotionCount = 0; // 해당 날짜에 이 감정이 나타난 총 횟수 (SUM 함수의 대상)

    @Column(name = "level_sum", nullable = false)
    private int levelSum = 0; // 해당 날짜에 이 감정의 레벨 총합 (평균 계산용)


}
