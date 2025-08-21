package com.jh.emotion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.emotion.dto.UserPreferenceInitialRequestDto;
import com.jh.emotion.dto.UserPreferenceResponseDto;
import com.jh.emotion.entity.User;
import com.jh.emotion.entity.UserPreference;
import com.jh.emotion.enums.PreferenceCategory;
import com.jh.emotion.enums.PreferenceType;
import com.jh.emotion.repository.UserClickEventRepository;
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
    private final UserClickEventRepository userClickEventRepository;

    // INITIAL(초기입력) 유저 선호도 입력 (배열로 여러개 받아서 한번에 저장)
    @Transactional(readOnly = false)
    public void saveUserPreference(List<UserPreferenceInitialRequestDto> userPreferenceInitialDto, Long userId) {
        // 유저 아이디 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 유저 선호도 정보 저장 
        for (UserPreferenceInitialRequestDto preference : userPreferenceInitialDto) {
            for (String genre : preference.getGenres()) {
                // 중복 체크
                if (userPreferenceRepository.existsByUser_UserIdAndCategoryAndGenre(userId, preference.getCategory(), genre)) {
                    continue; // 이미 있으면 저장하지 않음
                }
                UserPreference userPreference = new UserPreference();
                userPreference.setUser(user);
                userPreference.setCategory(preference.getCategory());
                userPreference.setGenre(genre);
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
            userPreferenceDto.setGenre(userPreference.getGenre());
            userPreferenceDto.setType(userPreference.getType().toString());
            userPreferenceDto.setUseCount(userPreference.getUseCount());
            userPreferenceDtos.add(userPreferenceDto);
        }

        return userPreferenceDtos;
    }


    // 유저 클릭 이벤트 기반 유저 선호도 자동 추가 (최근 30일 이내 클릭 이벤트 기준 동일 genre 5개 이상 조회시 유저 선호도 자동저장)
    // 일정시간 마다 추가 하게끔 자동 추가 기능 추가 필요.
    @Transactional(readOnly = false)
    public void addUserPreferenceByClickEvent(Long userId) {
        // 유저 아이디 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now(); //현재 날짜 기준 
        LocalDateTime thirthDayAgo = now.minusDays(30); // 현재 날짜 기준 -30 



        //현재날짜 기준 30일 이내 + 동일 genre 5개 이상 조회  Object[] 배열 : [type, genre, count]
        List<Object[]> userClickEvents = userClickEventRepository.findTypeGenreCountsByUserAndCreatedAtAfter(userId, thirthDayAgo, 5);


        for(Object[] userClickEvent : userClickEvents) {
            String type = (String) userClickEvent[0];
            String genre = (String) userClickEvent[1];
            int count = ((Number) userClickEvent[2]).intValue(); // 일단 사용 안하고 무시

            PreferenceCategory category;
            try {
                category = PreferenceCategory.valueOf(type);
            } catch (IllegalArgumentException e) {
                // 예상외의 카테골면 기타 ETC로 저장 
                category = PreferenceCategory.ETC;
            }

            if(userPreferenceRepository.existsByUser_UserIdAndCategoryAndGenre(userId, category, genre)) {  //중복체크
                continue; //유저 선호도 중복저장 제한 
            }

            UserPreference userPreference = new UserPreference();
            userPreference.setUser(user);
            userPreference.setCategory(category);
            userPreference.setGenre(genre);
            userPreference.setType(PreferenceType.DISCOVERED);
            userPreference.setUseCount(0); 
            userPreferenceRepository.save(userPreference);
        }
        
        
        









    }



}
