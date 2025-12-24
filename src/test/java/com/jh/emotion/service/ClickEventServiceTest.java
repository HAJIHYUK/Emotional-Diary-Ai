package com.jh.emotion.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jh.emotion.dto.UserClickEventDto;
import com.jh.emotion.entity.User;
import com.jh.emotion.repository.RecommendationRepository;
import com.jh.emotion.repository.UserClickEventRepository;
import com.jh.emotion.repository.UserPreferenceRepository;
import com.jh.emotion.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ClickEventServiceTest {

    @Mock private UserClickEventRepository userClickEventRepository;
    @Mock private UserRepository userRepository;
    @Mock private RecommendationRepository recommendationRepository;
    @Mock private UserPreferenceRepository userPreferenceRepository;

    @InjectMocks
    private ClickEventService clickEventService;

    @Test
    @DisplayName("이미 오늘 클릭한 추천 아이템인 경우 저장을 건너뛰어야 함")
    void duplicateClickTest() {
        // Given
        Long userId = 1L;
        Long recId = 100L;
        UserClickEventDto dto = new UserClickEventDto();
        dto.setRecommendationId(recId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        // 이미 존재함(true)을 반환하도록 설정
        when(userClickEventRepository.existsByUser_UserIdAndRecommendation_RecommendationIdAndCreatedAtBetween(
                anyLong(), anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        // When
        clickEventService.saveUserClickEvent(dto, userId);

        // Then
        // 저장이 호출되지 않아야 함 (exists가 true이므로 return 됨)
        verify(userClickEventRepository, never()).save(any());
    }
}
