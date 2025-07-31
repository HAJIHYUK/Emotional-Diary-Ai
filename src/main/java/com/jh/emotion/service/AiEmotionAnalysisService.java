package com.jh.emotion.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jh.emotion.dto.EmotionAnalysisResultDto;
import com.jh.emotion.dto.UserRecommendationResponseDto;
import com.jh.emotion.entity.DiaryRecord;
import com.jh.emotion.entity.Emotion;
import com.jh.emotion.entity.Recommendation;
import com.jh.emotion.entity.User;
import com.jh.emotion.entity.UserPreference;
import com.jh.emotion.enums.PreferenceType;
import com.jh.emotion.repository.DiaryRecordRepository;
import com.jh.emotion.repository.RecommendationRepository;
import com.jh.emotion.repository.UserPreferenceRepository;
import com.jh.emotion.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AiEmotionAnalysisService {


    private final DiaryRecordRepository diaryRecordRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;
    private final RecommendationRepository recommendationRepository;
    private final YoutubeService youtubeService;
    private final YoutubeAndNaverTypeClassificationService typeClassificationService;


    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent";
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Gemini 2.0 API를 사용하여 텍스트에서 감정을 분석합니다.
     * 
     * @param diaryId 감정 분석을 할 일기의 번호
     * 
     * @return 감정 분석 결과
     * @throws JsonProcessingException JSON 처리 중 오류 발생시
     * @throws HttpClientErrorException API 클라이언트 오류 발생시
     * @throws HttpServerErrorException API 서버 오류 발생시
     * @throws Exception 기타 예외 발생시
     */
    @Transactional(readOnly = false)
    public EmotionAnalysisResultDto analyzeEmotion(Long userId,Long diaryId) throws JsonProcessingException {
        log.info("[analyzeEmotion] 서비스에 전달된 diaryId: {}", diaryId);
        // URL에 API 키를 쿼리 파라미터로 추가
        String url = UriComponentsBuilder.fromHttpUrl(GEMINI_API_URL)
                .queryParam("key", apiKey)
                .toUriString();
        
        //유저 위치 조회
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found"));
        String userLocation = user.getLocation();

        DiaryRecord diaryRecord = diaryRecordRepository.findById(diaryId)//일기 번호로 일기 조회
        .orElseThrow(() -> new EntityNotFoundException("DiaryRecord not found"));
        log.info("[analyzeEmotion] 실제로 조회된 DiaryRecord id: {}", diaryRecord.getDiaryRecordId());

        String text = diaryRecord.getContent();//일기 내용
        String userPreference="";
        
        //유저 선호도 조회
        List<UserPreference> userPreferences = userPreferenceRepository.findByUser_UserId(userId);
        //조건에 맞는 유저 선호도 추출(initial )
        for(UserPreference pf : userPreferences) {
            if(pf.getType() == PreferenceType.INITIAL) {
                userPreference = pf.getCategory().toString()+ ":" + pf.getItemName();
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Request JSON 생성
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        // 컨텐츠 배열 생성
        ArrayNode contentsArray = requestBody.putArray("contents");
        ObjectNode content = contentsArray.addObject();
        
        // 파트 배열 생성
        ArrayNode partsArray = content.putArray("parts");
        ObjectNode textPart = partsArray.addObject();
        
        textPart.put("text",
    "text의 감정을 분석하고, 유저 위치와 유저 취향 정보를 참고해서 " +
    "취향에 맞는 3~6개, 취향 외의 것 2~4개를 추천해줘. " +
    "아래와 같이 카테고리별로 추천 기준을 반드시 지켜줘:\n" +
    "- CAFE, RESTAURANT, FOOD: 반드시 실제 네이버 플레이스/지도에서 검색 가능한 구체적인 종류/특징(예: '테라스 카페', '매운 갈비찜', '디저트 카페', '이탈리안 레스토랑')로만 추천해줘. '가성비', '저렴한', '분위기 좋은' 등 추상적 수식어는 절대 넣지 마. 반드시 실제 검색 가능한 키워드로만 추천해.\n" +
    "- YOUTUBE, ENTERTAINMENT, MOVIE, BOOK, MUSIC: 반드시 실제 존재하는 정확한 이름(정확한 영화/책/음악/채널명 등)으로만 추천해줘. 예를 들어, '기생충', 'BTS', '해리포터', 'Love Poem', '미움받을 용기'처럼 실제 검색 가능한 고유명사(정확한 제목/이름)만 추천해. '아이유 신나는 노래', '미스터리 스릴러 영화', '긍정 심리학 도서', '다큐멘터리 영화' 등 장르, 수식어, 추상적 표현은 절대 넣지 마.\n" +
    "- PLACE, WALKING_TRAIL, ACTIVITY: 반드시 '공원 산책로', '호수공원 산책로', '숲속 둘레길', '실내 클라이밍'처럼 장소의 **간단한 특징이나 종류, 활동의 구체적인 유형(실제 검색 가능한 키워드)만** 추천해줘. '도심 속', '아늑한', '자연과 함께하는' 등 불필요한 수식어나 추상적 표현은 절대 넣지 마. 위치(도시/동네 등)는 내부적으로만 참고하고, 추천 title, reason 등 모든 응답에 지역명(도시/동네 등)은 절대 포함하지 마.\n" +
    "- non_matching_preferences(취향 외의 것)는 '반대 개념'이 아니라, 평소 선호하지 않거나 감정상태에 따라 시도해볼 만한 다른 장르/종류로만 추천해줘. 예를 들어, 평소 액션 영화를 좋아하면 멜로나 공포영화 등 다른 장르를 추천해줘. 단, 이 경우에도 반드시 실제 존재하는 정확한 이름(고유명사)만 추천해.\n" +
    "반드시 아래 JSON 형식으로만 응답해. 다른 설명이나 텍스트는 절대 포함하지 마.\n" +
    "JSON형식:{emotion:주요감정(기쁨,슬픔,분노,불안,중립), intensity:감정강도(1-10), confidence:감정분석신뢰도(0-1), description:분석설명," +
    "recommendations:{matching_preferences:[{type, title, reason}], non_matching_preferences:[{type, title, reason}]}} " +
    "text:" + text + " 유저선호취향:" + userPreference + " 유저위치:" + userLocation
);
        
        // 생성 설정 추가
        ObjectNode generationConfig = requestBody.putObject("generationConfig");
        generationConfig.put("temperature", 0.1); // 창의성 조절 (낮을수록 일관성)
        generationConfig.put("topK", 1); 
        generationConfig.put("topP", 0.8);
        generationConfig.put("maxOutputTokens", 1024);
        
        String requestJson = objectMapper.writeValueAsString(requestBody);
        log.info("Gemini API 요청: {}", requestJson);
        
        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        log.info("Gemini API 응답 상태: {}", response.getStatusCode());
        String responseBody = response.getBody();
        log.debug("Gemini API 응답 본문: {}", responseBody);
        
        // 응답 파싱
        JsonNode jsonResponse = objectMapper.readTree(responseBody);
        
        // Gemini API 응답 구조에 맞게 파싱 (candidates -> content -> parts -> text)
        String content_text = jsonResponse.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        
        log.info("Gemini API 응답 컨텐츠: {}", content_text);
        
        // 마크다운 코드 블록 제거 (예: ```json, ```)
        String cleanedContent = content_text
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();
        
        log.info("정제된 JSON 컨텐츠: {}", cleanedContent);
        JsonNode result = objectMapper.readTree(cleanedContent); // cleanedContent는 순수 JSON 문자열
        EmotionAnalysisResultDto emotionAnalysisResultDto = saveEmotionAnalysisResult(diaryId, result); // 감정 분석 결과 저장 및 감정분석 결과 반환
        saveRecommendations(diaryId, result); // 추천 정보 저장
        // JSON 응답 파싱 및 반환
        return emotionAnalysisResultDto;
    }


    // 감저 분석 결과 저장 및 감정분석 결과 반환
    @Transactional(readOnly = false)
    public EmotionAnalysisResultDto saveEmotionAnalysisResult(Long diaryId, JsonNode result) {

        DiaryRecord diaryRecord = diaryRecordRepository.findById(diaryId)
        .orElseThrow(() -> new EntityNotFoundException("DiaryRecord not found"));

        Emotion emotion = new Emotion();
        emotion.setLabel(result.path("emotion").asText()); //path 사용 이유는 get()으로 받으면 npe 발생 할 수도 있음
        emotion.setLevel(result.path("intensity").asLong());
        emotion.setUser(diaryRecord.getUser());
        emotion.setConfidence(result.path("confidence").asDouble());
        emotion.setDescription(result.path("description").asText());
        

        diaryRecord.setEmotionAnalysisCount(diaryRecord.getEmotionAnalysisCount() + 1);
        diaryRecord.setEmotion(emotion);
        diaryRecordRepository.save(diaryRecord); //emotion 저장은 cascade 옵션으로 설정해두었음

        EmotionAnalysisResultDto emotionAnalysisResultDto = new EmotionAnalysisResultDto(emotion.getLabel(), emotion.getLevel(),
         emotion.getDescription(), emotion.getConfidence(), diaryRecord.getDiaryRecordId());

        return emotionAnalysisResultDto;

    }
    
    // 감정 분석 결과 조회
    public EmotionAnalysisResultDto getEmotionAnalysisResult(Long diaryId) {
        DiaryRecord diaryRecord = diaryRecordRepository.findById(diaryId)
        .orElseThrow(() -> new EntityNotFoundException("DiaryRecord not found"));

        Emotion emotion = diaryRecord.getEmotion();
        if (emotion == null) {
            throw new EntityNotFoundException("Emotion not found");
        }

        EmotionAnalysisResultDto emotionAnalysisResultDto = new EmotionAnalysisResultDto(emotion.getLabel(), emotion.getLevel(),
        emotion.getDescription(), emotion.getConfidence(), diaryRecord.getDiaryRecordId());

        return emotionAnalysisResultDto;
    }

    // 감정 분석 결과 추천 정보 저장
    @Transactional(readOnly = false)
    public void saveRecommendations(Long diaryId, JsonNode result) {
        DiaryRecord diaryRecord = diaryRecordRepository.findById(diaryId)
            .orElseThrow(() -> new EntityNotFoundException("DiaryRecord not found"));

        // 1. matching_preferences 저장
        JsonNode matching = result.path("recommendations").path("matching_preferences");
        if (matching.isArray()) {
            for (JsonNode rec : matching) {
                Recommendation recommendation = new Recommendation();
                recommendation.setUser(diaryRecord.getUser());
                recommendation.setDiaryRecord(diaryRecord);
                recommendation.setTypePreference("matching_preferences");
                recommendation.setType(rec.path("type").asText());
                recommendation.setTitle(rec.path("title").asText());
                recommendation.setReason(rec.path("reason").asText());
                recommendation.setLink(typeClassificationService.typeClassification(
                    rec.path("type").asText(),
                    rec.path("title").asText(),
                    diaryRecord.getUser().getUserId()
                )); //유튜브/네이버 검색 로직
                recommendationRepository.save(recommendation);
            }
        }

        // 2. non_matching_preferences 저장
        JsonNode nonMatching = result.path("recommendations").path("non_matching_preferences");
        if (nonMatching.isArray()) {
            for (JsonNode rec : nonMatching) {
                Recommendation recommendation = new Recommendation();
                recommendation.setUser(diaryRecord.getUser());
                recommendation.setDiaryRecord(diaryRecord);
                recommendation.setTypePreference("non_matching_preferences");
                recommendation.setType(rec.path("type").asText());
                recommendation.setTitle(rec.path("title").asText());
                recommendation.setReason(rec.path("reason").asText());
                recommendation.setLink(typeClassificationService.typeClassification(
                    rec.path("type").asText(),
                    rec.path("title").asText(),
                    diaryRecord.getUser().getUserId()
                )); //유튜브/네이버 검색 로직
                recommendationRepository.save(recommendation);
            }
        }
    }


    // 감정 분석 결과 추천정보 조회
    public List<UserRecommendationResponseDto> getRecommendations(Long diaryId) {
        

        List<Recommendation> recommendations = recommendationRepository.findByDiaryRecord_DiaryRecordId(diaryId);
        List<UserRecommendationResponseDto> userRecommendationResponseDtos = new ArrayList<>();

        for(Recommendation rec : recommendations) {
            // 값 확인용 로그 추가
            System.out.println("[추천정보] type: " + rec.getType() + ", title: " + rec.getTitle() + ", reason: " + rec.getReason() + ", link: " + rec.getLink() + ", typePreference: " + rec.getTypePreference());
            UserRecommendationResponseDto userRecommendationResponseDto = new UserRecommendationResponseDto();
            userRecommendationResponseDto.setDiaryRecord(diaryId);
            userRecommendationResponseDto.setTypePreference(rec.getTypePreference());
            userRecommendationResponseDto.setType(rec.getType());
            userRecommendationResponseDto.setTitle(rec.getTitle());
            userRecommendationResponseDto.setReason(rec.getReason());
            userRecommendationResponseDto.setLink(rec.getLink());
            userRecommendationResponseDtos.add(userRecommendationResponseDto);
        }

        return userRecommendationResponseDtos;
    }


    
//구글api로 ai가 추천하는 맛집이나 산책로같은거 받기 , 사용자들끼리 오늘 하루의 감정을 공유 하는 게시판 ..? 
// 감사일기..? 알아보기 ai가 뭔가 격려나 이런것도 해줄 수 있음 힘내라고나 잘하고있다고나 이런것들도 반환 받는것도 낫배드일듯? 
//움 ai api 요청할때 좀 간단히 요청해서 정확하게 받아오고 추가로 검색할때 쓸것들을 어떻게든 해야될듯..? 그니까 네이버 플레이스나 구글에 정확하게 검색하기 위해서 지역명은 빼야되고 그런식으로 추천받아야됨

}