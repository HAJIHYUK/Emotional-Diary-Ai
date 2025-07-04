package com.jh.emotion.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.emotion.dto.UserPreferenceInitialRequestDto;
import com.jh.emotion.dto.UserPreferenceResponseDto;
import com.jh.emotion.entity.User;
import com.jh.emotion.entity.UserPreference;
import com.jh.emotion.enums.PreferenceType;
import com.jh.emotion.repository.UserPreferenceRepository;
import com.jh.emotion.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;


    // INITIAL(초기입력) 유저 선호도 입력 (배열로 여러개 받아서 한번에 저장)
    @Transactional(readOnly = false)
    public void saveUserPreference(List<UserPreferenceInitialRequestDto> userPreferenceInitialDto, Long userId) {
        // 유저 아이디 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 유저 선호도 정보 저장 
        for (UserPreferenceInitialRequestDto preference : userPreferenceInitialDto) {
            for (String itemName : preference.getItemNames()) {
                UserPreference userPreference = new UserPreference();
                userPreference.setUser(user);
                userPreference.setCategory(preference.getCategory());
                userPreference.setItemName(itemName);
                userPreference.setType(PreferenceType.INITIAL);
                userPreference.setUseCount(0);
                userPreferenceRepository.save(userPreference);
            }
        }
    }

    //유저 선호도 조회
    public List<UserPreferenceResponseDto> getUserPreference(Long userId) {
        //유저 아이디 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        //유저 선호도 조회 
        List<UserPreference> userPreferences = userPreferenceRepository.findByUser_UserId(userId);

        List<UserPreferenceResponseDto> userPreferenceDtos = new ArrayList<>();

        for(UserPreference userPreference : userPreferences) {
            UserPreferenceResponseDto userPreferenceDto = new UserPreferenceResponseDto();
            userPreferenceDto.setCategory(userPreference.getCategory().toString());
            userPreferenceDto.setItemName(userPreference.getItemName());
            userPreferenceDto.setType(userPreference.getType().toString());
            userPreferenceDto.setUseCount(userPreference.getUseCount());
            userPreferenceDtos.add(userPreferenceDto);
        }

        return userPreferenceDtos;
    }

}
