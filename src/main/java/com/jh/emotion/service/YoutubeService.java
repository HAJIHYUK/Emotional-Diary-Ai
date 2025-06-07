package com.jh.emotion.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class YoutubeService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    //유튜브 실제 검색 후 링크반환 
    public String getTopYoutubeLink(String title, String reviewType) {
        String query = title + " " + reviewType;
        log.info("query: {}", query);
        String url = "https://www.googleapis.com/youtube/v3/search"
            + "?key=" + apiKey
            + "&q=" + query
            + "&part=snippet"
            + "&type=video"
            + "&maxResults=1";

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = objectMapper.readTree(response);
            JsonNode items = json.get("items");
            if (items != null && items.size() > 0) {
                JsonNode firstItem = items.get(0);
                String videoId = firstItem.get("id").get("videoId").asText();
                return "https://www.youtube.com/watch?v=" + videoId;
            }
        } catch (Exception e) {
            log.error("유튜브 API 파싱 오류", e);
        }
        return null;
    }

    //검색 타입 분류 로직 
    public String TypeClassification(String type, String title){
        String platform = ""; // "YOUTUBE", "NAVER", "GOOGLE", "KAKAO" 등
        String reviewType = "";
        switch(type.toUpperCase()){
            case "YOUTUBE":
            case "ENTERTAINMENT":
            case "MUSIC":
                platform = "YOUTUBE";
                reviewType = "";
                break;
            case "MOVIE":
                platform = "YOUTUBE";
                reviewType = "영화 리뷰";
                break;
            case "BOOK":
                platform = "YOUTUBE";
                reviewType = "책 리뷰";
                break;
            case "CAFE":
            case "RESTAURANT":
            case "PLACE":
            case "WALKING_TRAIL":
            case "ACTIVITY":
            case "FOOD":
                platform = "NAVER";
                reviewType = ""; // 필요시 네이버용 검색어 가공
                break;
            default:
                platform = "NONE";
                break;
        }
        if ("YOUTUBE".equals(platform)) {
            return getTopYoutubeLink(title, reviewType);
        } else if ("NAVER".equals(platform)) {
            return null;
        } else if ("GOOGLE".equals(platform)) {
            return null;
        } else {
            return null;
        }
    }

}
