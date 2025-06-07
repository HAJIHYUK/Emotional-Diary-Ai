package com.jh.emotion.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.emotion.dto.DiaryDetailDto;
import com.jh.emotion.dto.DiaryListDto;
import com.jh.emotion.dto.DiaryWriteDto;
import com.jh.emotion.entity.DiaryRecord;
import com.jh.emotion.entity.Emotion;
import com.jh.emotion.entity.User;
import com.jh.emotion.repository.DiaryRecordRepository;
import com.jh.emotion.repository.EmotionRepository;
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
    private final EmotionRepository emotionRepository;

    //일기 작성 후 최종저장
    @Transactional(readOnly = false)
    public DiaryRecord createDiaryRecord(DiaryWriteDto diaryWriteDto) {
    User user = userRepository.findById(diaryWriteDto.getUserId())
    .orElseThrow(() -> new EntityNotFoundException("User not found"));
    
    DiaryRecord diaryRecord = new DiaryRecord();
    diaryRecord.setUser(user);
    diaryRecord.setEmotion(null);
    diaryRecord.setDraft(false);
    diaryRecord.setEmotionAnalysisCount(0);
    diaryRecord.setContent(diaryWriteDto.getContent());
    diaryRecord.setWeather(diaryWriteDto.getWeather());
    diaryRecord.setEntryDate(diaryWriteDto.getEntryDate());
    diaryRecordRepository.save(diaryRecord);

    return diaryRecord;
    }


    


    //일기 목록 조회
    public List<DiaryListDto> getDiaryList(Long userId) {
    User user = userRepository.findById(userId)
    .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
    List<DiaryRecord> diaryRecords = diaryRecordRepository.findByUser_UserId(userId);
    List<DiaryListDto> diaryListDtos = new ArrayList<>();

    for(DiaryRecord diaryRecord : diaryRecords){
        DiaryListDto diaryListDto = new DiaryListDto();
        diaryListDto.setDiaryRecordId(diaryRecord.getDiaryRecordId());
        diaryListDto.setEntryDate(diaryRecord.getEntryDate());
        diaryListDto.setWeather(diaryRecord.getWeather());
        if(diaryRecord.getEmotion() == null){
            diaryListDto.setEmotionLabel("감정 분석 대기중");
        }else{
            diaryListDto.setEmotionLabel(diaryRecord.getEmotion().getLabel());
        }
        diaryListDto.setDraft(diaryRecord.isDraft());
        diaryListDto.setCreatedAt(diaryRecord.getCreatedAt());
        diaryListDtos.add(diaryListDto);
    }

        return diaryListDtos;
    }

    //일기 상세 조회
    public DiaryDetailDto getDiaryDetail(Long diaryId) {
        DiaryRecord diaryRecord = diaryRecordRepository.findById(diaryId)
            .orElseThrow(() -> new EntityNotFoundException("DiaryRecord not found"));

        DiaryDetailDto diaryDetailDto = new DiaryDetailDto();
        //일기 기본 정보 설정
        diaryDetailDto.setDiaryRecordId(diaryRecord.getDiaryRecordId());
        diaryDetailDto.setEntryDate(diaryRecord.getEntryDate());
        diaryDetailDto.setWeather(diaryRecord.getWeather());
        diaryDetailDto.setContent(diaryRecord.getContent());
        diaryDetailDto.setDraft(diaryRecord.isDraft()); //임시저장 여부 true면 임시저장, false면 최종저장
        diaryDetailDto.setEmotionAnalysisCount(diaryRecord.getEmotionAnalysisCount());
        diaryDetailDto.setCreatedAt(diaryRecord.getCreatedAt());
        diaryDetailDto.setUpdatedAt(diaryRecord.getUpdatedAt());

        Emotion emotion = diaryRecord.getEmotion();
        if (emotion != null) {
            emotion = emotionRepository.findById(emotion.getEmotionId()).orElse(null);
            diaryDetailDto.setEmotionLabel(emotion.getLabel());
            diaryDetailDto.setEmotionLevel(emotion.getLevel());
            diaryDetailDto.setEmotionDescription(emotion.getDescription());
            diaryDetailDto.setEmotionConfidence(emotion.getConfidence());
        } else {
            diaryDetailDto.setEmotionLabel("감정 분석 대기중");
            diaryDetailDto.setEmotionLevel(null);
            diaryDetailDto.setEmotionDescription(null);
            diaryDetailDto.setEmotionConfidence(null);
        }

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
        diaryRecordRepository.save(diaryRecord);
    }

    //일기 삭제
    @Transactional(readOnly = false)
    public String deleteDiaryRecord(Long diaryId) {
        diaryRecordRepository.deleteById(diaryId);
        return "일기가 성공적으로 삭제되었습니다.";
    }

}
