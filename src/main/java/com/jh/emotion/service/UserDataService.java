package com.jh.emotion.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.emotion.entity.User;
import com.jh.emotion.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserDataService {

 
    private final UserRepository userRepository;
    // 유저 위치 저장
    @Transactional(readOnly = false)
    public void saveUserLocation(Long userId, String location) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        user.setLocation(location);
        userRepository.save(user);
    }

    //유저 위치 조회
    public String getUserLocation(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return user.getLocation();
    }

    //유저 탈퇴 (Soft Delete)
    @Transactional(readOnly = false)
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        if (!user.isDeleted()) { // 이미 탈퇴 상태가 아니라면
            user.setDeleted(true);
            user.setDeletedAt(LocalDateTime.now()); // 탈퇴 시각 기록
            userRepository.save(user);
            log.info("사용자 탈퇴 처리 완료. userId: {}", userId);
        } else {
            log.warn("이미 탈퇴 처리된 사용자입니다. userId: {}", userId);
        }
    }
}