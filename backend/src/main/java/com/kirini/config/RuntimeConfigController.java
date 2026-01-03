package com.kirini.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// 간단한 런타임 설정 제공 컨트롤러
@RestController
public class RuntimeConfigController {

    // 공개 가능한 값만 여기에 넣기 (application.properties 또는 환경변수에서 주입)
    @Value("${frontend.apiBaseUrl:https://api.example.com}")
    private String apiBaseUrl;

    @Value("${frontend.featureFlag:false}")
    private boolean featureFlag;

    @GetMapping("/runtime-config")
    public Map<String, Object> runtimeConfig() {
        return Map.of(
            "apiBaseUrl", apiBaseUrl,
            "featureFlag", featureFlag
        );
    }
}


