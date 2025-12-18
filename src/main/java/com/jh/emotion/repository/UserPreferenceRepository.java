package com.jh.emotion.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.UserPreference;
import com.jh.emotion.enums.PreferenceCategory;
import com.jh.emotion.enums.PreferenceType;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    
    // 활성화된 취향만 조회
    List<UserPreference> findByUser_UserIdAndIsActiveTrue(Long userId);
    
    // 할성화된 취향 중복 체크
    boolean existsByUser_UserIdAndCategoryAndGenreAndIsActiveTrue(Long userId, PreferenceCategory category, String genre);
    
    // 비활성화된 추향을 찾기 위한 메소드
    Optional<UserPreference> findByUser_UserIdAndCategoryAndGenreAndIsActiveFalse(Long userId, PreferenceCategory category, String genre);

    // 특정 활성 취향을 Optional로 조회
    Optional<UserPreference> findByUser_UserIdAndCategoryAndGenreAndIsActiveTrue(Long userId, PreferenceCategory category, String genre);

    // 발견된 취향중 활성화중인 취향을 마지막 사용일자 기준 조회 
    List<UserPreference> findByTypeAndIsActiveTrueAndLastUsedAtBefore(PreferenceType type, LocalDateTime cutoffDate);

    // [추가] 특정 유저의 모든 취향 조회 (활성/비활성 포함)
    List<UserPreference> findByUser(com.jh.emotion.entity.User user);
} 