package com.jh.emotion.service;

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


}
