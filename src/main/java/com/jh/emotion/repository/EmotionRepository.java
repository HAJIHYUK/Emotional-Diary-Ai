package com.jh.emotion.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jh.emotion.entity.Emotion;

@Repository
public interface EmotionRepository extends JpaRepository<Emotion,Long> {
    // 일기별 감정 리스트 조회
    List<Emotion> findByDiaryRecord_DiaryRecordId(Long diaryRecordId);
} 