package com.jh.emotion.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NaverSearchService {

    private final String clientId = "4CNSx2JMFpr0h7ljom3n";
    private final String clientSecret = "S9PDgHXxFm";

    // 네이버 검색 API를 통해 장소 링크 검색 ( 인스타 or 플레이스 링크 등등)
    public List<String> searchPlaceLinks(String query, int count, String location) {
        // 1. 네이버 지역 검색 API의 요청 URL을 생성 (검색어와 결과 개수 포함)
        String url = UriComponentsBuilder.fromHttpUrl("https://openapi.naver.com/v1/search/local.json")
                .queryParam("query", location+" "+query) // 검색어 파라미터 추가
                .queryParam("display", count) // 결과 개수 파라미터 추가
                .build().toUriString(); // 최종 URL 생성

        // 2. 네이버 API 인증을 위한 HTTP 헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId); // 클라이언트 아이디 추가
        headers.set("X-Naver-Client-Secret", clientSecret); // 클라이언트 시크릿 추가

        // 3. 헤더를 포함한 HttpEntity 객체 생성 (요청에 사용)
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 4. 외부 API 호출을 위한 RestTemplate 객체 생성
        RestTemplate restTemplate = new RestTemplate();

        // 5. 네이버 API에 GET 요청을 보내고, 응답을 Map 형태로 받음
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        // 6. 결과로 반환할 링크(문자열) 리스트 생성
        List<String> links = new ArrayList<>();

        // 7. 응답이 정상(200 OK)일 때만 결과 처리
        if (response.getStatusCode() == HttpStatus.OK) {
            // 7-1. 응답 JSON에서 "items" 배열 추출 (각 아이템이 장소 정보)
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
            for (Map<String, Object> item : items) {
                // 7-2. 각 장소의 "link" 필드 추출 (네이버 플레이스, 인스타, 홈페이지 등)
                String link = (String) item.get("link");
                // 7-3. 링크를 결과 리스트에 추가
                links.add(link);
            }
        }
        // 8. 최종적으로 링크 리스트 반환
        return links;
    }
}
