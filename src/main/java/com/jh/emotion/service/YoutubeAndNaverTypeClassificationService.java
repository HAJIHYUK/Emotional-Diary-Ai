package com.jh.emotion.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jh.emotion.entity.User;
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
    public String typeClassification(String type, String title, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        // user.getLocation() 등 필요시 사용
        String platform = ""; // "YOUTUBE", "NAVER", "GOOGLE", "KAKAO" 등
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
                reviewType = ""; // 필요시 네이버용 검색어 가공
                break;
            default:
                platform = "NONE";
                break;
        }
        if ("YOUTUBE".equals(platform)) {
            return youtubeService.getTopYoutubeLink(title, reviewType);
        } else if ("NAVER".equals(platform)) {
            String link = naverSearchService.selectBestLink(naverSearchService.searchPlaceLinks(title, 5, user.getLocation()));
            if (link != null) {
                return link;
            }
            // fallback: 수원으로 재검색
            link = naverSearchService.selectBestLink(naverSearchService.searchPlaceLinks(title, 5, "수원"));
            if (link != null) {
                return link;
            }
            return "NO_LINK";
        } else if ("GOOGLE".equals(platform)) {
            return null;
        } else {
            return null;
        }
    }
}
