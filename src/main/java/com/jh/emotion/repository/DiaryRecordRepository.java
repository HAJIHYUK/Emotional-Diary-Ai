package com.jh.emotion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jh.emotion.entity.DiaryRecord;

@Repository
public interface DiaryRecordRepository extends JpaRepository<DiaryRecord,Long> {


    List<DiaryRecord> findByUser_UserId(Long userId);
} 