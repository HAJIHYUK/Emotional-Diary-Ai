package com.jh.emotion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        for (UserPreferenceInitialRequestDto preferenceDto : userPreferenceInitialDto) {
            for (String genre : preferenceDto.getGenres()) {
                // 이미 활성화된 동일 취향이 있는지 확인
                if (userPreferenceRepository.existsByUser_UserIdAndCategoryAndGenreAndIsActiveTrue(userId, preferenceDto.getCategory(), genre)) {
                    continue; // 이미 활성화 상태이면 건너뜀
                }

                // 비활성화된 동일 취향이 있는지 확인
                Optional<UserPreference> inactivePreference = userPreferenceRepository.findByUser_UserIdAndCategoryAndGenreAndIsActiveFalse(userId, preferenceDto.getCategory(), genre);

                if (inactivePreference.isPresent()) {
                    // 있으면 재활성화
                    UserPreference userPreference = inactivePreference.get();
                    userPreference.setActive(true);
                } else {
                    // 없으면 새로 생성
                    UserPreference newUserPreference = new UserPreference();
                    newUserPreference.setUser(user);
                    newUserPreference.setCategory(preferenceDto.getCategory());
                    newUserPreference.setGenre(genre);
                    newUserPreference.setType(PreferenceType.INITIAL);
                    newUserPreference.setUseCount(0);
                    userPreferenceRepository.save(newUserPreference);
                }
            }
        }
    }

    //유저 선호도 조회
    public List<UserPreferenceResponseDto> getUserPreference(Long userId) {
        //유저 아이디 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        //유저 선호도 조회 
        List<UserPreference> userPreferences = userPreferenceRepository.findByUser_UserIdAndIsActiveTrue(userId);

        List<UserPreferenceResponseDto> userPreferenceDtos = new ArrayList<>();

        for(UserPreference userPreference : userPreferences) {
            UserPreferenceResponseDto userPreferenceDto = new UserPreferenceResponseDto();
            userPreferenceDto.setUserPreferenceId(userPreference.getUserPreferenceId());
            userPreferenceDto.setCategory(userPreference.getCategory().toString());
            userPreferenceDto.setGenre(userPreference.getGenre());
            userPreferenceDto.setType(userPreference.getType().toString());
            userPreferenceDto.setUseCount(userPreference.getUseCount());
            userPreferenceDto.setActive(userPreference.isActive());
            userPreferenceDtos.add(userPreferenceDto);
        }

        return userPreferenceDtos;
    }

    //유저 선호도 비활성화 (Hard Delete -> Soft Delete)
    @Transactional(readOnly = false)
    public void deactivateUserPreference(Long userId, List<Long> userPreferenceIds) {
        //유저 아이디 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        for(Long upid : userPreferenceIds) {
            Optional<UserPreference> optionalPreference = userPreferenceRepository.findById(upid);
            if (optionalPreference.isPresent()) {
                UserPreference userPreference = optionalPreference.get();
                // 자기 자신의 취향이 맞는지 한번 더 확인
                if (userPreference.getUser().getUserId().equals(userId)) {
                    userPreference.setActive(false);
                }
            }
        }
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

            if(userPreferenceRepository.existsByUser_UserIdAndCategoryAndGenreAndIsActiveTrue(userId, category, genre)) {  //중복체크
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
