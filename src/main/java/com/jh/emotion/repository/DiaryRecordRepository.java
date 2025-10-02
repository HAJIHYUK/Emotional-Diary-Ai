package com.jh.emotion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.DiaryRecord;

@Repository
public interface DiaryRecordRepository extends JpaRepository<DiaryRecord, Long> {
    
    List<DiaryRecord> findByUser_UserId(Long userId);

    @Query("SELECT dr FROM DiaryRecord dr LEFT JOIN FETCH dr.emotions WHERE dr.diaryRecordId = :diaryId")
    DiaryRecord findWithEmotionsById(@Param("diaryId") Long diaryId);

    // 자기 자신을 제외하고 동일한 해시값을 가진 가장 최신의 DiaryRecord를 찾음 (L2 캐싱용 버그 수정: 자기호출 문제 해결)
    Optional<DiaryRecord> findTopByContentHashAndDiaryRecordIdNotOrderByCreatedAtDesc(String contentHash, Long diaryRecordId);
} 