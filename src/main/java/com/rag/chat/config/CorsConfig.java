package com.rag.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Value("${spring.web.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Value("${spring.web.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${spring.web.cors.allowed-headers:Authorization,Content-Type,X-Request-Id}")
    private String allowedHeaders;

    @Value("${spring.web.cors.exposed-headers:X-RateLimit-Limit,X-RateLimit-Remaining,Retry-After}")
    private String exposedHeaders;

    @Value("${spring.web.cors.allow-credentials:true}")
    private boolean allowCredentials;

    /**
     * Cors Filter to handle cross-origin
     * @return
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        config.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        config.setExposedHeaders(Arrays.asList(exposedHeaders.split(",")));
        config.setAllowCredentials(allowCredentials);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}