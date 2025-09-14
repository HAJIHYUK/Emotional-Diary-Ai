package com.jh.emotion.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.emotion.dto.DiaryDetailDto;
import com.jh.emotion.dto.DiaryListDto;
import com.jh.emotion.dto.DiaryWriteDto;
import com.jh.emotion.dto.EmotionDto;
import com.jh.emotion.entity.DiaryRecord;
import com.jh.emotion.entity.Emotion;
import com.jh.emotion.entity.User;
import com.jh.emotion.repository.DiaryRecordRepository;
import com.jh.emotion.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRecordRepository diaryRecordRepository;
    private final UserRepository userRepository;

    //일기 작성 후 최종저장
    @Transactional(readOnly = false)
    public DiaryRecord createDiaryRecord(DiaryWriteDto diaryWriteDto) {
        User user = userRepository.findById(diaryWriteDto.getUserId())
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String contentHash = HashService.generateContentHash(diaryWriteDto.getContent());
        DiaryRecord diaryRecord = new DiaryRecord();
        diaryRecord.setUser(user);
        diaryRecord.setDraft(false);
        diaryRecord.setEmotionAnalysisCount(0);
        diaryRecord.setContent(diaryWriteDto.getContent());
        diaryRecord.setWeather(diaryWriteDto.getWeather());
        diaryRecord.setEntryDate(diaryWriteDto.getEntryDate());
        diaryRecord.setContentHash(contentHash);
        diaryRecordRepository.save(diaryRecord);

        return diaryRecord;
    }

    //일기 목록 조회.. 
    public List<DiaryListDto> getDiaryList(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<DiaryRecord> diaryRecords = diaryRecordRepository.findByUser_UserId(userId);
        List<DiaryListDto> diaryListDtos = new ArrayList<>();

        for (DiaryRecord diaryRecord : diaryRecords) {
            DiaryListDto diaryListDto = new DiaryListDto();
            diaryListDto.setDiaryRecordId(diaryRecord.getDiaryRecordId());
            diaryListDto.setEntryDate(diaryRecord.getEntryDate());
            diaryListDto.setWeather(diaryRecord.getWeather());
            diaryListDto.setContent(diaryRecord.getContent()); 

            // 감정 리스트 변환 
            List<EmotionDto> emotionDtos = new ArrayList<>();
            List<Emotion> emotions = diaryRecord.getEmotions();
            if (emotions != null && !emotions.isEmpty()) {
                for (Emotion emotion : emotions) {
                    emotionDtos.add(new EmotionDto(
                        emotion.getLabel(),
                        emotion.getLevel(),
                        emotion.getDescription(),
                        emotion.getConfidence(),
                        emotion.getRatio()
                    ));
                }
            }
            diaryListDto.setEmotions(emotionDtos); // emotions 리스트 설정

            diaryListDto.setDraft(diaryRecord.isDraft());
            diaryListDto.setCreatedAt(diaryRecord.getCreatedAt());
            diaryListDtos.add(diaryListDto);
        }
        return diaryListDtos;
    }

    //일기 상세 조회
    public DiaryDetailDto getDiaryDetail(Long diaryId) {
        DiaryRecord diaryRecord = diaryRecordRepository.findWithEmotionsById(diaryId);
        if (diaryRecord == null) {
            throw new EntityNotFoundException("DiaryRecord not found");
        }

        DiaryDetailDto diaryDetailDto = new DiaryDetailDto();
        //일기 기본 정보 설정
        diaryDetailDto.setDiaryRecordId(diaryRecord.getDiaryRecordId());
        diaryDetailDto.setEntryDate(diaryRecord.getEntryDate());
        diaryDetailDto.setWeather(diaryRecord.getWeather());
        diaryDetailDto.setContent(diaryRecord.getContent());
        diaryDetailDto.setDraft(diaryRecord.isDraft());
        diaryDetailDto.setEmotionAnalysisCount(diaryRecord.getEmotionAnalysisCount());
        diaryDetailDto.setCreatedAt(diaryRecord.getCreatedAt());
        diaryDetailDto.setUpdatedAt(diaryRecord.getUpdatedAt());
        diaryDetailDto.setAiComment(diaryRecord.getAiComment());

        // 감정 리스트 변환
        List<EmotionDto> emotionDtos = new ArrayList<>();
        List<Emotion> emotions = diaryRecord.getEmotions();
        if (emotions != null && !emotions.isEmpty()) {
            for (Emotion emotion : emotions) {
                emotionDtos.add(new EmotionDto(
                    emotion.getLabel(),
                    emotion.getLevel(),
                    emotion.getDescription(),
                    emotion.getConfidence(),
                    emotion.getRatio()
                ));
            }
        }
        diaryDetailDto.setEmotions(emotionDtos);
        return diaryDetailDto;
    }

    //일기 상세 수정
    @Transactional(readOnly = false)
    public void updateDiaryRecord(Long diaryId, DiaryWriteDto diaryWriteDto) {
        DiaryRecord diaryRecord = diaryRecordRepository.findById(diaryId)
            .orElseThrow(() -> new EntityNotFoundException("DiaryRecord not found"));
        //일기 내용 수정
        diaryRecord.setContent(diaryWriteDto.getContent());
        diaryRecord.setWeather(diaryWriteDto.getWeather());
        diaryRecord.setEntryDate(diaryWriteDto.getEntryDate());
        // 내용이 변경되었으므로 해시도 업데이트
        diaryRecord.setContentHash(HashService.generateContentHash(diaryWriteDto.getContent()));
        diaryRecordRepository.save(diaryRecord);
    }

    //일기 삭제
    @Transactional(readOnly = false)
    public String deleteDiaryRecord(Long diaryId) {
        diaryRecordRepository.deleteById(diaryId);
        return "일기가 성공적으로 삭제되었습니다.";
    }

    
}
