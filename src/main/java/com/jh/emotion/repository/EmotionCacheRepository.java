package com.jh.emotion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.EmotionCache;

@Repository
public interface EmotionCacheRepository extends JpaRepository<EmotionCache,Long> {

} 