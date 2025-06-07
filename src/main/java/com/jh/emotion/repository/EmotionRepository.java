package com.jh.emotion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.Emotion;

@Repository
public interface EmotionRepository extends JpaRepository<Emotion,Long> {

} 