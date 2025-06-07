package com.jh.emotion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.Recommendation;
import java.util.List;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation,Long> {

    // DiaryRecord 엔티티로 추천 정보 조회
    List<Recommendation> findByDiaryRecord(com.jh.emotion.entity.DiaryRecord diaryRecord);
    // diaryRecordId(Long)로 추천 정보 조회
    List<Recommendation> findByDiaryRecord_DiaryRecordId(Long diaryRecordId);
} 