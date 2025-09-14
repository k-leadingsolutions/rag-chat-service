package com.rag.chat;

import com.rag.chat.config.LoggingAspectProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableConfigurationProperties(LoggingAspectProperties.class)
public class RagChatServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RagChatServiceApplication.class, args);
    }
}