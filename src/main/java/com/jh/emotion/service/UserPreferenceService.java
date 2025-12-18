package com.jh.emotion.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
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

    /**
     * 사용자의 취향 설정을 동기화하는 메서드 
     */
    @Transactional(readOnly = false)
    public void saveUserPreference(List<UserPreferenceInitialRequestDto> userPreferenceDtos, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 1. 프론트에서 전달받은 최종 취향 목록을 Set으로 변환
        Set<String> newPreferencesSet = new HashSet<>();
        for (UserPreferenceInitialRequestDto dto : userPreferenceDtos) {
            for (String genre : dto.getGenres()) {
                newPreferencesSet.add(dto.getCategory().name() + "::" + genre);
            }
        }

        // 2. DB에 저장된 해당 유저의 모든 취향 목록을 Map으로 조회
        List<UserPreference> dbPreferences = userPreferenceRepository.findByUser(user);
        Map<String, UserPreference> dbPreferencesMap = new HashMap<>();
        for (UserPreference pref : dbPreferences) {
            dbPreferencesMap.put(pref.getCategory().name() + "::" + pref.getGenre(), pref);
        }

        // 3. [비활성화 처리] DB에는 있지만 새 목록에는 없는 취향을 찾아 비활성화
        for (UserPreference dbPref : dbPreferencesMap.values()) {
            String key = dbPref.getCategory().name() + "::" + dbPref.getGenre();
            if (dbPref.isActive() && !newPreferencesSet.contains(key)) {
                log.info("비활성화 처리: userId={}, category={}, genre={}", userId, dbPref.getCategory(), dbPref.getGenre());
                dbPref.setActive(false);
            }
        }

        // 4. [추가/활성화 처리] 새 목록에 있는 취향을 기준으로 추가 또는 활성화
        for (UserPreferenceInitialRequestDto dto : userPreferenceDtos) {
            for (String genre : dto.getGenres()) {
                String key = dto.getCategory().name() + "::" + genre;
                UserPreference dbPref = dbPreferencesMap.get(key);

                if (dbPref != null) { // DB에 취향이 이미 존재하면
                    if (!dbPref.isActive()) { // 비활성 상태일 경우, 활성화로 변경
                        log.info("활성화 처리: userId={}, category={}, genre={}", userId, dto.getCategory(), genre);
                        dbPref.setActive(true);
                    }
                } else { // DB에 취향이 아예 없으면, 새로 생성 (INITIAL 타입으로)
                    log.info("신규 추가 처리: userId={}, category={}, genre={}", userId, dto.getCategory(), genre);
                    UserPreference newUserPreference = new UserPreference();
                    newUserPreference.setUser(user);
                    newUserPreference.setCategory(dto.getCategory());
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
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

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
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        for(Long upid : userPreferenceIds) {
            Optional<UserPreference> optionalPreference = userPreferenceRepository.findById(upid);
            if (optionalPreference.isPresent()) {
                UserPreference userPreference = optionalPreference.get();
                if (userPreference.getUser().getUserId().equals(userId)) {
                    userPreference.setActive(false);
                }
            }
        }
    }

    // 유저 클릭 이벤트 기반 유저 선호도 자동 추가
    @Transactional(readOnly = false)
    public void addUserPreferenceByClickEvent(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirthDayAgo = now.minusDays(30);

        List<Object[]> userClickEvents = userClickEventRepository.findTypeGenreCountsByUserAndCreatedAtAfter(userId, thirthDayAgo, 5);

        for(Object[] userClickEvent : userClickEvents) {
            String type = (String) userClickEvent[0];
            String genre = (String) userClickEvent[1];

            PreferenceCategory category;
            try {
                category = PreferenceCategory.valueOf(type);
            } catch (IllegalArgumentException e) {
                category = PreferenceCategory.ETC;
            }

            if(userPreferenceRepository.existsByUser_UserIdAndCategoryAndGenreAndIsActiveTrue(userId, category, genre)) {
                continue;
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

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void scheduleAddUserPreferenceByClickEvent() {
        log.info("===== 사용자 클릭 이벤트 기반 취향 자동 발견 스케줄러 시작 =====");
        List<User> allUsers = userRepository.findAll();

        if (allUsers.isEmpty()) {
            log.info("등록된 사용자가 없어 스케줄러를 종료합니다.");
            return;
        }

        for (User user : allUsers) {
            try {
                log.info("사용자 ID: {} 취향 발견 로직 실행", user.getUserId());
                addUserPreferenceByClickEvent(user.getUserId());
            } catch (Exception e) {
                log.error("사용자 ID: {} 취향 발견 로직 실행 중 오류 발생", user.getUserId(), e);
            }
        }
        log.info("===== 사용자 클릭 이벤트 기반 취향 자동 발견 스케줄러 종료 =====");
    }

    @Scheduled(cron = "0 0 5 * * SUN")
    @Transactional
    public void scheduleDeactivateStalePreferences() {
        log.info("===== 오래된 '발견된 취향' 자동 비활성화 스케줄러 시작 =====");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        List<UserPreference> targets = userPreferenceRepository.findByTypeAndIsActiveTrueAndLastUsedAtBefore(PreferenceType.DISCOVERED, cutoffDate);

        if (targets.isEmpty()) {
            log.info("비활성화할 대상 취향이 없습니다. 스케줄러를 종료합니다.");
            return;
        }

        log.info("총 {}개의 오래된 취향을 비활성화합니다.", targets.size());
        for (UserPreference preference : targets) {
            preference.setActive(false);
            log.debug("비활성화 완료: userId={}, category={}, genre={}",
                preference.getUser().getUserId(), preference.getCategory(), preference.getGenre());
        }
        log.info("===== 오래된 '발견된 취향' 자동 비활성화 스케줄러 종료 =====");
    }
}