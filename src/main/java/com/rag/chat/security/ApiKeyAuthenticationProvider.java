package com.rag.chat.security;

import lombok.Getter;

import java.util.Set;
import java.util.Collections;

public class ApiKeyAuthenticationProvider {

    private final Set<String> validApiKeys;

    @Getter
    private final String apiKeyHeaderName;

    /**
     * API Keys Handler
     * @param validApiKeys
     * @param apiKeyHeaderName
     */
    public ApiKeyAuthenticationProvider(Set<String> validApiKeys, String apiKeyHeaderName) {
        this.validApiKeys = Collections.unmodifiableSet(validApiKeys);
        this.apiKeyHeaderName = apiKeyHeaderName != null ? apiKeyHeaderName : "x-api-key";
    }

    public boolean isValid(String apiKey) {
        return apiKey != null && validApiKeys.contains(apiKey);
    }

}