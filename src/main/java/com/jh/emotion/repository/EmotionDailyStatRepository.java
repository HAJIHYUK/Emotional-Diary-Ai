package com.jh.emotion.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.EmotionDailyStat;

@Repository
public interface EmotionDailyStatRepository extends JpaRepository<EmotionDailyStat, Long> {

    // 특정 유저의 특정 날짜, 특정 감정에 대한 통계 데이터를 찾는 메서드
    Optional<EmotionDailyStat> findByUser_UserIdAndDateAndEmotionLabel(Long userId, LocalDate date, String emotionLabel);


    @Query("SELECT e.emotionLabel, SUM(e.emotionCount) as totalCount, SUM(e.levelSum) as totalLevelSum " +
         "FROM EmotionDailyStat e " +
         "WHERE e.user.userId = :userId AND e.date BETWEEN :startDate AND :endDate " +
         "GROUP BY e.emotionLabel")
    List<Object[]> getEmotionStatsForPeriod(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
