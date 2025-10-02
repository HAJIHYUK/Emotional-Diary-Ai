package com.jh.emotion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.emotion.dto.LinkInfo; // LinkInfo import
import com.jh.emotion.dto.UserRecommendationResponseDto;
import com.jh.emotion.entity.DiaryRecord;
import com.jh.emotion.entity.Recommendation;
import com.jh.emotion.repository.DiaryRecordRepository;
import com.jh.emotion.repository.RecommendationRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final DiaryRecordRepository diaryRecordRepository;
    private final YoutubeAndNaverTypeClassificationService typeClassificationService;

    /**
     * 감정 분석 결과 추천 정보 저장 (수정됨)
     */
    @Transactional
    public void saveRecommendations(Long diaryId, JsonNode result) {
        DiaryRecord diaryRecord = diaryRecordRepository.findById(diaryId)
            .orElseThrow(() -> new EntityNotFoundException("DiaryRecord not found"));
        
        JsonNode recommendationsNode = result.path("recommendations");

        // 1. matching_preferences 저장
        JsonNode matching = recommendationsNode.path("matching_preferences");
        if (matching.isArray()) {
            for (JsonNode rec : matching) {
                // typeClassification 호출하여 linkInfo 가져옴
                LinkInfo linkInfo = typeClassificationService.typeClassification(
                    rec.path("type").asText(),
                    rec.path("title").asText(),
                    diaryRecord.getUser().getUserId()
                );

                Recommendation recommendation = new Recommendation();
                recommendation.setUser(diaryRecord.getUser());
                recommendation.setDiaryRecord(diaryRecord);
                recommendation.setTypePreference("matching_preferences");
                recommendation.setType(rec.path("type").asText());
                recommendation.setTitle(rec.path("title").asText());
                recommendation.setReason(rec.path("reason").asText());
                recommendation.setGenre(rec.path("genre").asText());
                
                //linkInfo에서 link와 linkType 설정
                recommendation.setLink(linkInfo.getLink());
                recommendation.setLinkType(linkInfo.getLinkType());
                
                recommendationRepository.save(recommendation);
            }
        }
        // 2. non_matching_preferences 저장
        JsonNode nonMatching = recommendationsNode.path("non_matching_preferences");
        if (nonMatching.isArray()) {
            for (JsonNode rec : nonMatching) {
                LinkInfo linkInfo = typeClassificationService.typeClassification(
                    rec.path("type").asText(),
                    rec.path("title").asText(),
                    diaryRecord.getUser().getUserId()
                );

                Recommendation recommendation = new Recommendation();
                recommendation.setUser(diaryRecord.getUser());
                recommendation.setDiaryRecord(diaryRecord);
                recommendation.setTypePreference("non_matching_preferences");
                recommendation.setType(rec.path("type").asText());
                recommendation.setTitle(rec.path("title").asText());
                recommendation.setReason(rec.path("reason").asText());
                recommendation.setGenre(rec.path("genre").asText());

                recommendation.setLink(linkInfo.getLink());
                recommendation.setLinkType(linkInfo.getLinkType());

                recommendationRepository.save(recommendation);
            }
        }
    }

    /**
     * 특정 일기의 추천 정보 조회 (수정됨)
     */
    public List<UserRecommendationResponseDto> getRecommendations(Long diaryId) {
        List<Recommendation> recommendations = recommendationRepository.findByDiaryRecord_DiaryRecordId(diaryId);
        List<UserRecommendationResponseDto> userRecommendationResponseDtos = new ArrayList<>();
        for (Recommendation rec : recommendations) {
            UserRecommendationResponseDto userRecommendationResponseDto = new UserRecommendationResponseDto();
            userRecommendationResponseDto.setDiaryRecord(diaryId);
            userRecommendationResponseDto.setTypePreference(rec.getTypePreference());
            userRecommendationResponseDto.setType(rec.getType());
            userRecommendationResponseDto.setTitle(rec.getTitle());
            userRecommendationResponseDto.setReason(rec.getReason());
            userRecommendationResponseDto.setLink(rec.getLink());
            userRecommendationResponseDto.setGenre(rec.getGenre());
            userRecommendationResponseDto.setRecommendationId(rec.getRecommendationId());

            if (rec.getLinkType() != null) {
                userRecommendationResponseDto.setLinkType(rec.getLinkType().name());
            }
            
            userRecommendationResponseDtos.add(userRecommendationResponseDto);
        }
        return userRecommendationResponseDtos;
    }

    /**
     * 최근 추천 정보 20개 조회 (중복 제거)
     */
    public List<String> getRecentRecommendations(Long userId) {
        List<String> recentTitles = recommendationRepository.findRecentRecommendationsByUserId(userId, PageRequest.of(0, 30));
        return recentTitles.stream().distinct().limit(20).toList();
    }
}