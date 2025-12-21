package com.jh.emotion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.DiaryRecord;

@Repository
public interface DiaryRecordRepository extends JpaRepository<DiaryRecord, Long> {
    
    // N+1 문제 해결을 위한 Fetch Join (EntityGraph 사용)
    // 일기 목록 조회 시 감정(emotions)까지 한 번에 가져옴 + 삭제되지 않은 일기만 조회
    @EntityGraph(attributePaths = {"emotions"})
    @Query("SELECT d FROM DiaryRecord d WHERE d.user.userId = :userId AND d.deleted = false ORDER BY d.entryDate DESC")
    List<DiaryRecord> findAllWithEmotionsByUser_UserId(@Param("userId") Long userId);

    List<DiaryRecord> findByUser_UserId(Long userId);

    // N+1 문제 해결 위한 상세 조회용 (EntityGraph 사용 -> Left Outer Join 보장), JOIN Fetch 사용해서 emotion 테이블에 데이터가 없으면 diary행 자체에서 결과가 바져 버림 
    @EntityGraph(attributePaths = {"emotions"})
    Optional<DiaryRecord> findByDiaryRecordId(Long diaryRecordId);

    // 자기 자신을 제외하고 동일한 해시값을 가진 가장 최신의 DiaryRecord를 찾음 (L2 캐싱용 버그 수정: 자기호출 문제 해결)
    Optional<DiaryRecord> findTopByContentHashAndDiaryRecordIdNotOrderByCreatedAtDesc(String contentHash, Long diaryRecordId);
} 