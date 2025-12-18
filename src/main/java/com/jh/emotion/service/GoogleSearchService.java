package com.jh.emotion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class GoogleSearchService {

    // 구글 API 방식은 생각보다 결과가 너무 많이 안나옴 ㅠㅠ..

    /*
    // 구글 API 방식에 사용되던 필드들을 모두
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
    */

    //생성자에서 더 이상 외부 의존성을 필요로 하지 않으므로, 매개변수 없는 기본 생성자를 사용합니다.
    public GoogleSearchService() {}

    /**
     * 메소드의 역할을 Google 검색 '페이지 URL'을 반환하는 것으로 변경합니다.
     * 내부적으로는 새로 추가된 createGoogleSearchUrl 헬퍼 메소드를 호출합니다.
     * @param query 검색할 문자열
     * @return 생성된 Google 검색 페이지 URL
     */
    public String getFirstSearchResultLink(String query) {
        log.info("[GoogleSearch] 구글 검색 URL 생성을 시작합니다. Query: '{}'", query);
        String generatedUrl = createGoogleSearchUrl(query);
        if (generatedUrl != null) {
            log.info("[GoogleSearch] URL 생성 성공! 생성된 URL: {}", generatedUrl);
        } else {
            log.warn("[GoogleSearch] URL 생성 실패. Query: '{}'", query);
        }
        return generatedUrl;
    }

    /**
     *  Google 검색 URL을 생성하는 헬퍼 메소드
     * @param query 검색할 문자열
     * @return 생성된 Google 검색 URL, 인코딩 실패나 입력값 오류 시 null
     */
    private String createGoogleSearchUrl(String query) {
        if (query == null || query.trim().isEmpty()) {
            return null;
        }
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            return "https://www.google.com/search?q=" + encodedQuery;
        } catch (UnsupportedEncodingException e) {
            log.error("URL 인코딩에 실패했습니다. Query: {}", query, e);
            return null;
        }
    }

    /*
    //  Google Custom Search API 호출 로직 전체를 주석 처리(사용해 봤는데 반환값이 예상외 결과가 너무 많이 나옴 )
    public String getFirstSearchResultLink_ApiVersion(String query) {
        log.info("[GoogleSearch] 구글 검색을 시작합니다. Query: '{}'", query);
        try {
            String url = UriComponentsBuilder.fromHttpUrl(GOOGLE_CUSTOM_SEARCH_URL)
                    .queryParam("key", apiKey)
                    .queryParam("cx", searchEngineId)
                    .queryParam("q", query)
                    .queryParam("num", 1)
                    .toUriString();

            log.debug("[GoogleSearch] 요청 URL: {}", url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode items = root.path("items");

            if (items.isArray() && !items.isEmpty()) {
                String firstLink = items.get(0).path("link").asText(null);
                log.info("[GoogleSearch] 검색 성공! 가져온 링크: {}", firstLink);
                return firstLink;
            } else {
                log.warn("[GoogleSearch] API는 성공적으로 호출했으나, 검색 결과에 'items'가 없습니다. Query: '{}'", query);
                return null;
            }

        } catch (IOException e) {
            log.error("[GoogleSearch] API 호출 또는 결과 파싱 중 IOException 발생. Query: '{}'", query, e);
        } catch (Exception e) {
            log.error("[GoogleSearch] 처리 중 알 수 없는 오류 발생. Query: '{}'", query, e);
        }

        return null;
    }
    */
}
