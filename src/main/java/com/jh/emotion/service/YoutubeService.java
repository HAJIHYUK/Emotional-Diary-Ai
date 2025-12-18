package com.jh.emotion.service;

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

    private final ApiKeyManagerService apiKeyManager;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    

    //유튜브 실제 검색 후 링크반환 
    public String getTopYoutubeLink(String title, String reviewType) {
        String apiKey = apiKeyManager.getApiKey("YOUTUBE");
        String query = title + " " + reviewType;
        log.info("query: {}", query);
        String url = "https://www.googleapis.com/youtube/v3/search"
            + "?key=" + apiKey
            + "&q=" + query
            + "&part=snippet"
            + "&type=video"
            + "&maxResults=3";

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode json = objectMapper.readTree(response);
            JsonNode items = json.get("items");
            if (items != null && items.isArray() && items.size() > 0) {
                for (JsonNode item : items) {
                    JsonNode idNode = item.get("id");
                    if (idNode != null && idNode.has("videoId")) {
                        String videoId = idNode.get("videoId").asText();
                        if (videoId != null && !videoId.isEmpty()) {
                            return "https://www.youtube.com/watch?v=" + videoId;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("유튜브 API 파싱 오류", e);
        }
        return null;
    }


}
