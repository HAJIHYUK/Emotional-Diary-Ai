package com.jh.emotion.service;

// (추가) URL 인코딩을 위한 클래스를 가져옵니다. 공백이나 한글을 URL 표준에 맞게 변환합니다.
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.emotion.dto.LinkInfo;
import com.jh.emotion.entity.User;
import com.jh.emotion.enums.LinkType;
import com.jh.emotion.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class YoutubeAndNaverTypeClassificationService {

    private final YoutubeService youtubeService;
    private final NaverSearchService naverSearchService;
    private final UserRepository userRepository;
    

    // AI감정분석후 추천 받은 후 추천받은 검색 타입 별로 분류 로직 
    // 예를들어 영화 리뷰 추천 받았으면 영화 리뷰 타입으로 분류 후 유튜브 서비스로 유튜브 검색
    // 예를들어 카페 추천 받았으면 카페 타입으로 분류 후 네이버 서비스로 네이버 검색
    public LinkInfo typeClassification(String type, String title, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        String platform = "";
        String reviewType = "";
        switch(type.toUpperCase()){
            case "YOUTUBE":
            case "ENTERTAINMENT":
                platform = "YOUTUBE";
                reviewType = "";
                break;
            case "MUSIC":
                platform = "YOUTUBE";
                reviewType = "음원";
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
                reviewType = "";
                break;
            default:
                platform = "GOOGLE";
                break;
        }

        String finalLink = null;

        if ("YOUTUBE".equals(platform)) {
            finalLink = youtubeService.getTopYoutubeLink(title, reviewType);

            //YouTube 검색 결과가 null이면 Google 검색으로 대체
            if (finalLink == null) {
                log.warn("YouTube 검색 결과가 없어 Google 검색으로 대체합니다. Query: {}", title + " " + reviewType);
                String query = title + (reviewType.isEmpty() ? "" : " " + reviewType); // (추가) '음원', '영화 리뷰' 같은 reviewType을 검색어에 포함시킵니다.
                finalLink = createGoogleSearchUrl(query); // (추가) 아래에 새로 추가된 헬퍼 메소드를 호출하여 Google 검색 URL을 생성합니다.
            }

        } else if ("NAVER".equals(platform)) {
            // 1차: '동' 단위로 검색
            String link = naverSearchService.selectBestLink(naverSearchService.searchPlaceLinks(title, 10, user.getLocation()));
            
            if (link != null && !link.contains("smartstore") && !link.contains("blog")) {
                finalLink = link;
            } else {
                // 2차: '시' 단위로 재검색 (예: "수원")
                // TODO: "수원" 하드코딩 부분을 사용자 위치 기반으로 동적으로 변경 필요
                finalLink = naverSearchService.selectBestLink(naverSearchService.searchPlaceLinks(title, 10, "수원"));
            }
            
            // (추가) Naver 검색이 최종적으로 실패하면 Google 검색으로 대체하는 로직입니다.
            if (finalLink == null) {
                log.warn("Naver 검색 결과가 없어 Google 검색으로 대체합니다. Query: {}", user.getLocation() + " " + title); // (추가) 검색 실패 시, 어떤 검색어로 대체 검색을 시도하는지 로그를 남깁니다.
                String query = user.getLocation() + " " + title; // (추가) 사용자 위치와 추천 제목을 조합하여 지역 기반 검색어를 만듭니다.
                finalLink = createGoogleSearchUrl(query); // (추가) 아래에 새로 추가된 헬퍼 메소드를 호출하여 Google 검색 URL을 생성합니다.
            }

        } else if ("GOOGLE".equals(platform)) {
            // (추가) case default에서 platform이 'GOOGLE'로 설정된 경우를 처리합니다.
            log.warn("정의되지 않은 플랫폼 '{}'으로 Google 검색을 시도합니다. Query: {}", type, title); // (추가) 알 수 없는 타입이 들어왔을 때, 어떤 타입이었는지 로그를 남깁니다.
            finalLink = createGoogleSearchUrl(title); // (추가) 이 경우엔 위치 정보 없이 제목만으로 Google 검색 URL을 생성합니다.
        }
        
        // 최종적으로 결정된 링크와 해당 링크의 타입을 분석하여 반환합니다。
        return new LinkInfo(finalLink, determineLinkType(finalLink));
    }

    /**
     * @param query 검색할 문자열 (예: "수원 감성카페")
     * @return 생성된 Google 검색 URL. 인코딩 실패나 입력값 오류 시 null을 반환합니다.
     */
    private String createGoogleSearchUrl(String query) {
        if (query == null || query.trim().isEmpty()) { //검색어가 비어 있는경우 불필요한 작업을 막기 위해 null을 반환
            return null;
        }
        try { // URLEncoder.encode가 발생시킬 수 있는 예외(UnsupportedEncodingException)를 처리하기 위한 try-catch 처리
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString()); 
            log.info("encodedQuery: {}", "https://www.google.com/search?q="+encodedQuery);
            return "https://www.google.com/search?q=" + encodedQuery;
        } catch (UnsupportedEncodingException e) { 
            log.error("URL 인코딩에 실패했습니다. Query: {}", query, e);
            return null;
        }
    }

    private LinkType determineLinkType(String url) {
        if (url == null || url.isEmpty()) {
            return LinkType.GENERIC;
        }
        if (url.contains("youtube") || url.contains("youtu.be")) {
            return LinkType.YOUTUBE;
        }
        if (url.contains("map.naver")) {
            return LinkType.NAVER_PLACE;
        }
        if (url.contains("blog.naver")) {
            return LinkType.NAVER_BLOG;
        }
        if (url.contains("instagram")) {
            return LinkType.INSTAGRAM;
        }
        if (url.contains("tistory.com") || url.contains("brunch.co.kr") || url.contains("news.")) {
            return LinkType.ARTICLE;
        }
        // (추가) Google 검색 URL('google.com/search')을 식별하여 GOOGLE_PLACE 타입으로 분류하기 위한 조건입니다.
        if (url.contains("google.com/search") || url.contains("google.com/maps")) {
            return LinkType.GOOGLE_PLACE;
        }
        return LinkType.GENERIC;
    }
}
