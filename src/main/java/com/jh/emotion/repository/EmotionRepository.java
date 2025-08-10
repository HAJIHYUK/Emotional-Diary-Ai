package com.jh.emotion.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.Emotion;

@Repository
public interface EmotionRepository extends JpaRepository<Emotion,Long> {
    // 일기별 감정 리스트 조회
    List<Emotion> findByDiaryRecord_DiaryRecordId(Long diaryRecordId);

    //일기날짜(entryDate) 기준으로 유저별 감정 통계 조회 (날짜 범위 , 감정 라벨 별 통계)
    @Query("SELECT e.label, COUNT(e), AVG(e.level) FROM Emotion e WHERE e.user.userId = :userId AND e.diaryRecord.entryDate >= :start AND e.diaryRecord.entryDate < :end GROUP BY e.label")
    List<Object[]> getEmotionStats(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);
//| label | count | avgLevel |
// |-------|-------|----------|
// | 기쁨 | 3 | 7.0 | (7+8+6)/3
// | 슬픔 | 1 | 4.0 |
// | 분노 | 1 | 5.0 |

} 