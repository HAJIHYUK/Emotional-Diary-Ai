package com.jh.emotion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.UserPreference;
import com.jh.emotion.enums.PreferenceCategory;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    List<UserPreference> findByUser_UserIdAndIsActiveTrue(Long userId);
    boolean existsByUser_UserIdAndCategoryAndGenreAndIsActiveTrue(Long userId, PreferenceCategory category, String genre);
    Optional<UserPreference> findByUser_UserIdAndCategoryAndGenreAndIsActiveFalse(Long userId, PreferenceCategory category, String genre);
} 