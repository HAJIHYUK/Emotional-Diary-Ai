package com.jh.emotion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.UserPreference;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference,Long> {

    //유저 아이디로 유저 선호도 리스트 조회
    List<UserPreference> findByUser_UserId(Long userId);

    boolean existsByUser_UserIdAndCategoryAndGenre(Long userId, com.jh.emotion.enums.PreferenceCategory category, String genre);
} 