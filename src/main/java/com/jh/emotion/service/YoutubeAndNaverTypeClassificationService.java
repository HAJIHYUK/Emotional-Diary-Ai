package com.jh.emotion.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.emotion.dto.LinkInfo;
import com.jh.emotion.entity.User;
import com.jh.emotion.enums.LinkType;
import com.jh.emotion.repository.UserRepository;
import com.jh.emotion.service.GoogleSearchService; // (추가) GoogleSearchService 주입

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
    private final GoogleSearchService googleSearchService; // (추가) GoogleSearchService 주입

    // AI감정분석후 추천 받은 후 추천받은 검색 타입 별로 분류 로직 
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
            
            if (finalLink == null) {
                log.warn("YouTube 검색 결과가 없어 Google 첫 번째 검색 결과를 가져옵니다. Query: {}", title + " " + reviewType);
                String query = title + (reviewType.isEmpty() ? "" : " " + reviewType);
                finalLink = googleSearchService.getFirstSearchResultLink(query); // (수정) URL 생성 대신 서비스 호출
            }

        } else if ("NAVER".equals(platform)) {
            String link = naverSearchService.selectBestLink(naverSearchService.searchPlaceLinks(title, 10, user.getLocation()));
            
            if (link != null && !link.contains("smartstore") && !link.contains("blog")) {
                finalLink = link;
            } else {
                finalLink = naverSearchService.selectBestLink(naverSearchService.searchPlaceLinks(title, 10, "수원"));
            }
            
            if (finalLink == null) {
                log.warn("Naver 검색 결과가 없어 Google 첫 번째 검색 결과를 가져옵니다. Query: {}", user.getLocation() + " " + title);
                String query = user.getLocation() + " " + title;
                finalLink = googleSearchService.getFirstSearchResultLink(query); // (수정) URL 생성 대신 서비스 호출
            }

        } else if ("GOOGLE".equals(platform)) {
            log.warn("정의되지 않은 플랫폼 '{}'으로 Google 첫 번째 검색 결과를 가져옵니다. Query: {}", type, title);
        finalLink = googleSearchService.getFirstSearchResultLink(title); // (수정) Google API 호출 서비스 사용
            log.info("[Fallback] Google 검색 결과: {}", finalLink != null ? finalLink : "결과 없음"); // (추가) Google 서비스로부터 받은 최종 링크를 기록합니다.
        }
        
        return new LinkInfo(finalLink, determineLinkType(finalLink));
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
        if (url.contains("google")) { 
            return LinkType.GOOGLE_PLACE;
        }
        return LinkType.GENERIC;
    }
}
