package com.jh.emotion.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Service
public class GoogleSearchService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${google.custom-search.api.key}")
    private String apiKey;

    @Value("${google.custom-search.cx}")
    private String searchEngineId;

    private static final String GOOGLE_CUSTOM_SEARCH_URL = "https://www.googleapis.com/customsearch/v1";

    public GoogleSearchService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Google Custom Search API를 호출하여 검색 결과의 첫 번째 링크를 반환합니다.
     * @param query 검색할 문자열
     * @return 검색 결과 첫 번째 링크 URL, 결과가 없거나 오류 발생 시 null
     */
    public String getFirstSearchResultLink(String query) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(GOOGLE_CUSTOM_SEARCH_URL)
                    .queryParam("key", apiKey)
                    .queryParam("cx", searchEngineId)
                    .queryParam("q", query)
                    .queryParam("num", 1) // 첫 번째 결과만 필요하므로 1개만 요청
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode items = root.path("items");

            if (items.isArray() && !items.isEmpty()) {
                // 검색 결과 중 첫 번째 아이템의 링크를 반환
                return items.get(0).path("link").asText(null);
            }

        } catch (IOException e) {
            log.error("Google Custom Search API 호출 또는 결과 파싱 중 오류 발생. Query: {}", query, e);
        } catch (Exception e) {
            log.error("Google Custom Search 처리 중 알 수 없는 오류 발생. Query: {}", query, e);
        }

        return null; // 결과가 없거나 오류 발생 시 null 반환
    }
}