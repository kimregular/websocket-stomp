package com.example.chatserver.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/chat/sse/unread")
                .allowedOrigins("http://localhost:3000") // 프론트 도메인
                .allowedMethods("GET")
                .allowCredentials(true);
    }
}
