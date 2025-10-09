package com.jh.emotion.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jh.emotion.dto.EmotionAnalysisResultDto;
import com.jh.emotion.dto.EmotionDto;
import com.jh.emotion.entity.DiaryRecord;
import com.jh.emotion.entity.Emotion;
import com.jh.emotion.entity.EmotionDailyStat;
import com.jh.emotion.entity.User;
import com.jh.emotion.entity.UserPreference;
import com.jh.emotion.enums.PreferenceType;
import com.jh.emotion.repository.DiaryRecordRepository;
import com.jh.emotion.repository.EmotionDailyStatRepository;
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
    private final CachingService cachingService;
    private final RecommendationService recommendationService; 
    private final EmotionDailyStatRepository emotionDailyStatRepository;
    

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent";
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 감정 분석 및 추천 생성을 총괄하는 메서드
     * @param userId 유저 ID
     * @param diaryId 일기 ID
     * @return 감정 분석 결과 DTO
     * @throws JsonProcessingException JSON 파싱 예외
     */
    @Transactional
    public EmotionAnalysisResultDto analyzeEmotionAndRecommend(Long userId, Long diaryId) throws JsonProcessingException {
        // 1. 감정 분석
        EmotionAnalysisResultDto emotionAnalysisResult = analyzeAndSaveEmotions(userId, diaryId);

        // 2. 추천 생성 및 저장
        generateAndSaveRecommendations(userId, diaryId, emotionAnalysisResult.getEmotions());

        return emotionAnalysisResult;
    }

    /**
     * 감정 분석을 수행하고 결과를 저장하는 메서드 (하이브리드 캐싱 적용)
     * 1. L1 캐시 (Redis) 조회
     * 2. L2 캐시 (DB의 contentHash) 조회
     * 3. Gemini API 호출 하여 감정 분석 결과 반환
     * @param userId 유저 ID
     * @param diaryId 일기 ID
     * @return 감정 분석 결과 DTO
     * @throws JsonProcessingException JSON 파싱 예외
     */
    @Transactional
    public EmotionAnalysisResultDto analyzeAndSaveEmotions(Long userId, Long diaryId) throws JsonProcessingException {
        DiaryRecord diaryRecord = diaryRecordRepository.findById(diaryId)
            .orElseThrow(() -> new EntityNotFoundException("DiaryRecord not found"));
        String text = diaryRecord.getContent();


        // 1. L1(Redis) 캐시 조회
        String cachedResult = cachingService.getCachedResult(text);
        if (cachedResult != null) {
            JsonNode emotionResultNode = objectMapper.readTree(cachedResult);
            String hash = HashService.generateContentHash(text);
            log.info("L1 캐시 조회 결과: {}", cachedResult);
            return saveEmotionAnalysisResult(diaryId, emotionResultNode, hash);
        }

        // 2. L2 캐시 (DB contentHash) 조회 (셀프 호출문제 해결 O : Repository 수정)
        String hash = HashService.generateContentHash(text);
        // 자기 자신을 제외하고 동일한 해시를 가진 다른 레코드를 찾도록 수정
        Optional<DiaryRecord> existingDiaryRecordOpt = diaryRecordRepository.findTopByContentHashAndDiaryRecordIdNotOrderByCreatedAtDesc(hash, diaryId);

        if (existingDiaryRecordOpt.isPresent()) { // 존재시 에만 캐시에 저장!
            DiaryRecord existingRecord = existingDiaryRecordOpt.get();
            ObjectNode resultNode = objectMapper.createObjectNode(); // 추후 DTO (POJO)로 변경 해보기
            ArrayNode emotionsNode = resultNode.putArray("emotions");
            for (Emotion e : existingRecord.getEmotions()) {
                ObjectNode emotionNode = emotionsNode.addObject();
                emotionNode.put("label", e.getLabel());
                emotionNode.put("level", e.getLevel());
                emotionNode.put("confidence", e.getConfidence());
                emotionNode.put("description", e.getDescription());
                emotionNode.put("ratio", e.getRatio());
            }
            resultNode.put("comment", existingRecord.getAiComment());

            // L1 캐시에 저장
            cachingService.setCachedResult(text, objectMapper.writeValueAsString(resultNode));
            log.info("L2 캐시 조회 결과: {}", objectMapper.writeValueAsString(resultNode));
            // 새로운 다이어리에 결과 저장 (해시는 저장할 필요 없음, 이미 동일 콘텐츠 존재)
            return saveEmotionAnalysisResult(diaryId, resultNode, hash);
        }
        
        // 3. API 호출하여 감정 분석
        JsonNode emotionResultNode = callGeminiForEmotionAnalysis(text);
        cachingService.setCachedResult(text, objectMapper.writeValueAsString(emotionResultNode)); 

        // 감정 분석 결과 및 해시 저장
        return saveEmotionAnalysisResult(diaryId, emotionResultNode, hash);
    }
    
    /**
     * Gemini API를 호출하여 텍스트 감정을 분석하는 메서드
     * @param text 분석할 텍스트
     * @return 분석 결과 JsonNode
     * @throws JsonProcessingException JSON 파싱 예외
     */
    private JsonNode callGeminiForEmotionAnalysis(String text) throws JsonProcessingException {
        String url = UriComponentsBuilder.fromHttpUrl(GEMINI_API_URL)
                .queryParam("key", apiKey)
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contentsArray = requestBody.putArray("contents");
        ObjectNode content = contentsArray.addObject();
        ArrayNode partsArray = content.putArray("parts");
        ObjectNode textPart = partsArray.addObject();
        textPart.put("text",
            "text의 감정을 분석하고 감정은 여러개(1~5개)일 수 있어 감정은 기쁨,슬픔,분노,불안,놀람,역겨움,중립이 있고 감정level:0~10 confidence은 너의 분석신뢰도 이고 ratio은 감정 비율맞춰서 분석해." +
            "반드시 아래 JSON 형식으로만 응답해. 다른 설명이나 텍스트는 절대 포함하지 마. comment 필드는 필수적으로 포함해야 해.\n" +
            "JSON형식:{emotions:[{label,level,confidence,description,ratio}], comment:\"일기에 대한 긍정적이고 따뜻한 코멘트 한 문장 작성.\"} " +
            "text:" + text
        );

        ObjectNode generationConfig = requestBody.putObject("generationConfig");
        generationConfig.put("temperature", 0.1);
        generationConfig.put("topK", 1);
        generationConfig.put("topP", 0.8);
        generationConfig.put("maxOutputTokens", 1024);

        String requestJson = objectMapper.writeValueAsString(requestBody);
        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        String content_text = jsonResponse.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        
        
        String cleanedContent = content_text
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();
        return objectMapper.readTree(cleanedContent);
    }

    /**
     * 분석된 감정을 기반으로 추천을 생성하고 저장하는 메서드
     * @param userId 유저 ID
     * @param diaryId 일기 ID
     * @param emotions 감정 DTO 리스트
     * @throws JsonProcessingException JSON 파싱 예외
     */
    @Transactional
    public void generateAndSaveRecommendations(Long userId, Long diaryId, List<EmotionDto> emotions) throws JsonProcessingException {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        String userLocation = user.getLocation();

        // 유저 선호도 조회
        String userPreference = "";
        List<UserPreference> userPreferences = userPreferenceRepository.findByUser_UserIdAndIsActiveTrue(userId);
        for (UserPreference pf : userPreferences) {
            if (pf.getType() == PreferenceType.INITIAL) {
                userPreference += pf.getCategory().toString() + ":" + pf.getGenre() + ", ";
            }
        }

        // 최근 20개 추천 정보 제외용 타이틀 생성 (RecommendationService에 위임)
        List<String> recentRecommendations = recommendationService.getRecentRecommendations(userId);
        String excludeRecommendationTitle = String.join(",", recentRecommendations);

        // Gemini API 호출하여 추천 생성
        JsonNode recommendationResultNode = callGeminiForRecommendations(emotions, userPreference, userLocation, excludeRecommendationTitle);

        // 추천 정보 저장 (RecommendationService에 위임)
        recommendationService.saveRecommendations(diaryId, recommendationResultNode);
    }

    /**
     * Gemini API를 호출하여 콘텐츠를 추천하는 메서드
     * @param emotions 감정 DTO 리스트
     * @param userPreference 유저 선호도 문자열
     * @param userLocation 유저 위치 문자열
     * @param excludeRecommendationTitle 제외할 추천 제목 문자열
     * @return 추천 결과 JsonNode
     * @throws JsonProcessingException JSON 파싱 예외
     */
    private JsonNode callGeminiForRecommendations(List<EmotionDto> emotions, String userPreference, String userLocation, String excludeRecommendationTitle) throws JsonProcessingException {
        String url = UriComponentsBuilder.fromHttpUrl(GEMINI_API_URL)
                .queryParam("key", apiKey)
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode requestBody = objectMapper.createObjectNode();
        ArrayNode contentsArray = requestBody.putArray("contents");
        ObjectNode content = contentsArray.addObject();
        ArrayNode partsArray = content.putArray("parts");
        ObjectNode textPart = partsArray.addObject();
        
        StringBuilder emotionStringBuilder = new StringBuilder();
        for (int i = 0; i < emotions.size(); i++) {
            EmotionDto e = emotions.get(i);
            emotionStringBuilder.append(e.getLabel()).append("(level:").append(e.getLevel()).append(")");
            if (i < emotions.size() - 1) {
                emotionStringBuilder.append(", ");
            }
        }
        String emotionString = emotionStringBuilder.length() > 0 ? emotionStringBuilder.toString() : "N/A";

        textPart.put("text",
            "분석된 감정을 기반으로, 유저 위치와 유저 취향 정보를 참고해서 " +
            "취향에 맞는 3~6개, 취향 외의 것 2~4개를 추천해줘." +
            "추천 정보는 꼭!! 최근 20개 추천 정보 제외 추천 정보를 추천해줘." +
            "최근20개:"+ excludeRecommendationTitle + // 최근 20개 추천 정보 제외
            "아래와 같이 카테고리별로 추천 기준을 반드시 지켜줘:\n" +
            "type(대분류)기준 ex: MOVIE, MUSIC, CAFE, RESTAURANT, FOOD, YOUTUBE, ENTERTAINMENT, PLACE, WALKING_TRAIL, ACTIVITY, non_matching_preferences\n" +
            "genre(소분류)기준 ex: ACTION, TERRACE, DESSERT 등 Cafe-Desert같이 이런거 말고 단일로 분류해줘 예를들어 MOVIE면 ACTION 같은거야 영어로만보내줘  " +
            "reson은 꼭 한국어로 설명해야되" +
            "- CAFE, RESTAURANT, FOOD: 반드시 실제 네이버 플레이스/지도에서 검색 가능한 구체적인 종류/특징(예: '테라스 카페', '매운 갈비찜', '디저트 카페', '이탈리안 레스토랑')로만 추천해줘.검색기반 엔진이기때문에 '따뜻한'등과 '가성비', '저렴한', '분위기 좋은' 등 추상적 수식어는 절대 넣지 마. 반드시 실제 검색 가능한 키워드로만 추천해.\n" +
            "- YOUTUBE, ENTERTAINMENT, MOVIE, BOOK, MUSIC: 반드시 실제 존재하는 정확한 이름(정확한 영화/책/음악/채널명 등)으로만 추천해줘. 예를 들어, '기생충', 'BTS', '해리포터', 'Love Poem', '미움받을 용기'처럼 실제 검색 가능한 고유명사(정확한 제목/이름)만 추천해. '아이유 신나는 노래', '미스터리 스릴러 영화', '긍정 심리학 도서', '다큐멘터리 영화' 등 장르, 수식어, 추상적 표현은 절대 넣지 마.\n" +
            "- PLACE, WALKING_TRAIL, ACTIVITY: 반드시 '공원 산책로', '호수공원 산책로', '숲속 둘레길', '실내 클라이밍'처럼 장소의 **간단한 특징이나 종류, 활동의 구체적인 유형(실제 검색 가능한 키워드)만** 추천해줘. '도심 속', '아늑한', '자연과 함께하는' 등 불필요한 수식어나 추상적 표현은 절대 넣지 마. 위치(도시/동네 등)는 내부적으로만 참고하고, 추천 title, reason 등 모든 응답에 지역명(도시/동네 등)은 절대 포함하지 마.단 WALKING_TRAIL 분류 호출은 지역기반으로 정확한 공원명이나 장소명을 알려줘 \n" +
            "- non_matching_preferences(취향 외의 것)는 '반대 개념'이 아니라, 평소 선호하지 않거나 감정상태에 따라 시도해볼 만한 다른 장르/종류로만 추천해줘. 예를 들어, 평소 액션 영화를 좋아하면 멜로나 공포영화 등 다른 장르를 추천해줘. 단, 이 경우에도 반드시 실제 존재하는 정확한 이름(고유명사)만 추천해.\n" +
            "반드시 아래 JSON 형식으로만 응답해. 다른 설명이나 텍스트는 절대 포함하지 마.\n" +
            "JSON형식:{recommendations:{matching_preferences:[{type, genre, title, reason}], non_matching_preferences:[{type, genre, title, reason}]}} " +
            "분석된감정:" + emotionString + " 유저선호취향:" + userPreference + " 유저위치:" + userLocation
        );
        ObjectNode generationConfig = requestBody.putObject("generationConfig");
        generationConfig.put("temperature", 0.1);
        generationConfig.put("topK", 1);
        generationConfig.put("topP", 0.8);
        generationConfig.put("maxOutputTokens", 1024);

        String requestJson = objectMapper.writeValueAsString(requestBody);
        HttpEntity<String> request = new HttpEntity<>(requestJson, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        String content_text = jsonResponse.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        String cleanedContent = content_text
                .replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();
        return objectMapper.readTree(cleanedContent);
    }


    //최근 20개 추천 정보 조회 (중복 제거) -> RecommendationService로 이동
    // public List<String> getRecentRecommendations(Long userId) { ... }
    

    /**
     * 감정 분석 결과 저장 및 감정분석 결과 반환
     * @param diaryId 일기 ID
     * @param result 감정 분석 결과 JsonNode
     * @param contentHash 일기 내용 해시
     * @return 감정 분석 결과 DTO
     */
    @Transactional(readOnly = false)
    public EmotionAnalysisResultDto saveEmotionAnalysisResult(Long diaryId, JsonNode result, String contentHash) {
        DiaryRecord diaryRecord = diaryRecordRepository.findById(diaryId)
            .orElseThrow(() -> new EntityNotFoundException("DiaryRecord not found"));

        // 감정 리스트 파싱 및 저장
        List<EmotionDto> emotionDtos = new ArrayList<>();
        List<Emotion> emotions = new ArrayList<>();

        // 중립 감정 비율을 조정하는 로직 추가
        JsonNode emotionsNode = adjustNeutralEmotionRatio(result.path("emotions"));
        
        if (emotionsNode.isArray()) {
            for (JsonNode emotionNode : emotionsNode) {
                Emotion emotion = new Emotion();
                emotion.setLabel(emotionNode.path("label").asText());
                emotion.setLevel(emotionNode.path("level").asLong());
                emotion.setConfidence(emotionNode.path("confidence").asDouble());
                emotion.setDescription(emotionNode.path("description").asText());
                if (emotionNode.has("ratio")) {
                    emotion.setRatio(emotionNode.path("ratio").asDouble());
                }
                emotion.setUser(diaryRecord.getUser());
                emotion.setDiaryRecord(diaryRecord);
                emotions.add(emotion);
                emotionDtos.add(new EmotionDto(
                    emotion.getLabel(),
                    emotion.getLevel(),
                    emotion.getDescription(),
                    emotion.getConfidence(),
                    emotion.getRatio()
                ));
            }
        }
        // 중복으로 AI 검색 분석 사용시 기존 감정 삭제 후 새로 저장 (덮어쓰기)
        diaryRecord.getEmotions().clear();
        diaryRecord.getEmotions().addAll(emotions);
        
        // comment 필드가 있는지 확인하고, 없으면 기본값 설정
        if (result.has("comment") && !result.get("comment").isNull()) {
            diaryRecord.setAiComment(result.path("comment").asText());
        } else {
            diaryRecord.setAiComment("AI 코멘트를 생성하지 못했습니다."); // 기본값 설정
        }

        diaryRecord.setEmotionAnalysisCount(diaryRecord.getEmotionAnalysisCount() + 1);
        if (contentHash != null) {
            diaryRecord.setContentHash(contentHash);
        }
        diaryRecordRepository.save(diaryRecord);
        updateDailyStats(diaryRecord, emotions);
        return new EmotionAnalysisResultDto(emotionDtos, diaryRecord.getDiaryRecordId());
    }

    // 감정 분석 결과 조회 (감정 리스트 반환)
    public EmotionAnalysisResultDto getEmotionAnalysisResult(Long diaryId) {
        DiaryRecord diaryRecord = diaryRecordRepository.findWithEmotionsById(diaryId);
        if (diaryRecord == null) {
            throw new EntityNotFoundException("DiaryRecord not found");
        }
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
        return new EmotionAnalysisResultDto(emotionDtos, diaryRecord.getDiaryRecordId());
    }



    /**
   * 감정 분석 결과를 바탕으로 일별 통계 테이블(EmotionDailyStat)을 업데이트함
   * @param diaryRecord 통계의 기준이 될 일기 엔티티 (entryDate, user 정보를 얻기 위함)
   * @param emotions 분석된 감정 엔티티 목록
   */
  @Transactional
  private void updateDailyStats(DiaryRecord diaryRecord, List<Emotion> emotions) {
      Map<String, int[]> updates = new HashMap<>();

      // 분석된 감정들을 Map으로 집계합니다.
      for (Emotion emotion : emotions) {
          String label = emotion.getLabel();

          // Map에 해당 감정 라벨(Key)이 있는지 확인합니다.
          if (!updates.containsKey(label)) {
              // 없으면, [횟수, 레벨 총합]을 담을 새로운 정수 배열을 만들어 Map에 넣습니다.
              updates.put(label, new int[]{0, 0});
          }

          // 이제 Map에 해당 라벨이 확실히 존재하므로, 값을 가져와서 업데이트합니다.
          int[] values = updates.get(label);
          values[0]++; // 횟수 1 증가
          values[1] += emotion.getLevel(); // 레벨 합산
      }

      // 2. 집계된 Map을 기반으로 DB에 업데이트하는 로직 (이 부분은 동일합니다)
      for (String label : updates.keySet()) {
        // 현재 키(label)에 해당하는 값([횟수, 레벨총합] 배열)을 가져옵니다.
        int[] values = updates.get(label);
        int countToAdd = values[0];
        int levelSumToAdd = values[1];
  
        // 기존 통계 데이터가 있는지 찾습니다.
        Optional<EmotionDailyStat> existingStatOpt = emotionDailyStatRepository
            .findByUser_UserIdAndDateAndEmotionLabel(diaryRecord.getUser().getUserId(), diaryRecord.getEntryDate(), label);
  
        EmotionDailyStat stat;
        if (existingStatOpt.isPresent()) {
            // 데이터가 있으면, 그 데이터를 가져옵니다.
            stat = existingStatOpt.get();
        } else {
            // 데이터가 없으면, 새로운 EmotionDailyStat 객체를 생성합니다.
            stat = new EmotionDailyStat();
            stat.setUser(diaryRecord.getUser());
            stat.setDate(diaryRecord.getEntryDate());
            stat.setEmotionLabel(label);
        }
  
        // 4. 횟수와 레벨 총합을 더해준 뒤 저장합니다.
        stat.setEmotionCount(stat.getEmotionCount() + countToAdd);
        stat.setLevelSum(stat.getLevelSum() + levelSumToAdd);
        emotionDailyStatRepository.save(stat);
    }
  }

    /**
     * AI가 분석한 감정 결과에서 '중립' 감정의 비율을 조정함
     * 중립 비율이 20%를 초과하면 20%로 낮추고, 초과분을 다른 감정들에 비례하여 분배합니다.
     * @param emotionsNode 감정 분석 결과가 담긴 JsonNode (배열 형태)
     * @return 조정된 감정 비율이 포함된 새로운 JsonNode
     */
    private JsonNode adjustNeutralEmotionRatio(JsonNode emotionsNode) {
        if (!emotionsNode.isArray() || emotionsNode.isEmpty()) {
            return emotionsNode;
        }

        List<ObjectNode> emotions = new ArrayList<>();
        for (JsonNode node : emotionsNode) {
            if (node.isObject()) {
                emotions.add((ObjectNode) node.deepCopy());
            }
        }
        
        log.info("중립 감정 비율 조정을 시작합니다. 원본 데이터: {}", emotionsNode.toString());

        ObjectNode neutralEmotion = null;
        for (ObjectNode e : emotions) {
            if ("중립".equalsIgnoreCase(e.path("label").asText("").trim())) {
                neutralEmotion = e;
                break;
            }
        }

        if (neutralEmotion == null) {
            log.warn("조정할 '중립' 감정을 찾지 못했습니다.");
            return emotionsNode;
        }

        double neutralRatio = neutralEmotion.path("ratio").asDouble(0.0);

        // 비율 값이 0.0 ~ 1.0 사이의 소수이므로, 20%는 0.2와 비교해야함
        if (neutralRatio <= 0.20) {
            log.info("'중립' 감정 비율({}%)이 20% 이하이므로 조정을 건너뜁니다.", neutralRatio * 100);
            return emotionsNode;
        }
        
        log.info("중립 감정 비율 조정 시작. Original ratio: {}%", neutralRatio * 100);

        double excessRatio = neutralRatio - 0.20;
        neutralEmotion.put("ratio", 0.20);

        List<ObjectNode> otherEmotions = new ArrayList<>();
        double otherEmotionsTotalRatio = 0.0;
        for (ObjectNode e : emotions) {
            if (!"중립".equalsIgnoreCase(e.path("label").asText("").trim())) {
                otherEmotions.add(e);
                otherEmotionsTotalRatio += e.path("ratio").asDouble(0.0);
            }
        }

        if (otherEmotionsTotalRatio > 0 && !otherEmotions.isEmpty()) {
            for (ObjectNode emotion : otherEmotions) {
                double originalRatio = emotion.path("ratio").asDouble(0.0);
                double proportion = originalRatio / otherEmotionsTotalRatio;
                double newRatio = originalRatio + (excessRatio * proportion);
                // 소수점 둘째 자리까지 반올림
                emotion.put("ratio", Math.round(newRatio * 100.0) / 100.0);
            }
            log.info("초과된 중립 감정 비율을 다른 감정에 분배했습니다.");
        } else {
            log.warn("중립 감정 외 다른 감정이 없거나 비율이 0이므로 초과분을 분배할 수 없습니다. 총 비율의 합이 100이 아닐 수 있습니다.");
        }

        double totalAdjustedRatio = 0.0;
        for (ObjectNode e : emotions) {
            totalAdjustedRatio += e.path("ratio").asDouble(0.0);
        }
        log.info("조정 후 전체 감정 비율 합계: {}", totalAdjustedRatio);

        ArrayNode adjustedEmotionsArray = objectMapper.createArrayNode();
        adjustedEmotionsArray.addAll(emotions);

        log.info("중립 감정 비율 조정 완료. 최종 데이터: {}", adjustedEmotionsArray.toString());

        return adjustedEmotionsArray;
    }
}