package com.jh.emotion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.DiaryRecord;

@Repository
public interface DiaryRecordRepository extends JpaRepository<DiaryRecord,Long> {
    List<DiaryRecord> findByUser_UserId(Long userId);

    // 감정 리스트를 fetch join으로 DiaryRecord와 함께 한 번에 가져오기
    @Query("SELECT d FROM DiaryRecord d LEFT JOIN FETCH d.emotions WHERE d.diaryRecordId = :diaryRecordId")
    DiaryRecord findWithEmotionsById(@Param("diaryRecordId") Long diaryRecordId);

    Optional<DiaryRecord> findByContentHash(String contentHash);
    
    Optional<DiaryRecord> findTopByContentHashOrderByCreatedAtDesc(String contentHash);
} 