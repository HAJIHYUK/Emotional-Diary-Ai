package com.jh.emotion.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "api.youtube") // 접두사가 'api.youtube'인 설정을 찾음 , properties 파일에 설정한 값을 주입받음
@Getter
@Setter
public class YouTubeConfig {


    /**
     * 'api.youtube.keys[0]', 'api.youtube.keys[1]'... 설정들이
     *  List<String> keys 필드에 순서대로 담기게 됨.
     */
    private List<String> keys;
}
