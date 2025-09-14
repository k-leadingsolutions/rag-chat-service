package com.rag.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SecurityService {
    /**
     * Input field sanitizer, to further secure app
     * Prevent XSS and Input Manipulation
     * Build a Threat KnowledgeBase (Audit)
     * @param input
     * @return
     */
    public static String sanitizeInput(String input) {
        if (input == null) return null;
        String sanitized = input
            .replaceAll("<script[^>]*>.*?</script>", "")
            .replaceAll("<[^>]+>", "")
            .trim();

        if (!sanitized.equals(input)) {
            log.warn("Input sanitization applied for security - original length: {}, sanitized length: {}", 
                    input.length(), sanitized.length());
        }
        return sanitized;
    }
}