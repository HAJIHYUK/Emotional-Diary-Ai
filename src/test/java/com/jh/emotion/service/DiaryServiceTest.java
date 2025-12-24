package com.jh.emotion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jh.emotion.dto.DiaryWriteDto;
import com.jh.emotion.entity.DiaryRecord;
import com.jh.emotion.entity.User;
import com.jh.emotion.repository.DiaryRecordRepository;
import com.jh.emotion.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    @Mock private DiaryRecordRepository diaryRecordRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private DiaryService diaryService;

    @Test
    @DisplayName("일기 작성 시 내용에 대한 해시값이 생성되고 저장되어야 함")
    void createDiaryRecordTest() {
        // Given
        Long userId = 1L;
        User user = new User();
        user.setUserId(userId);

        DiaryWriteDto dto = new DiaryWriteDto();
        dto.setUserId(userId);
        dto.setContent("오늘 날씨가 맑다.");
        dto.setWeather("맑음");
        dto.setEntryDate(LocalDate.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        DiaryRecord result = diaryService.createDiaryRecord(dto);

        // Then
        assertThat(result.getContent()).isEqualTo(dto.getContent());
        assertThat(result.getContentHash()).isNotNull(); // 해시값이 생성되었는지 확인
        assertThat(result.getContentHash()).hasSize(64); // SHA-256 해시는 64글자
        
        verify(diaryRecordRepository, times(1)).save(any(DiaryRecord.class));
    }
}
