package com.jh.emotion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.Recommendation;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation,Long> {

    // DiaryRecord 엔티티로 추천 정보 조회
    List<Recommendation> findByDiaryRecord(com.jh.emotion.entity.DiaryRecord diaryRecord);
    // diaryRecordId(Long)로 추천 정보 조회
    List<Recommendation> findByDiaryRecord_DiaryRecordId(Long diaryRecordId);
    // UserId(Long)로 최근 추천 정보 조회 (중복 제거는 서비스에서)
    @Query("SELECT r.title FROM Recommendation r WHERE r.user.userId = :userId ORDER BY r.createdAt DESC")
    List<String> findRecentRecommendationsByUserId(@Param("userId") Long userId, Pageable pageable);


    
} 