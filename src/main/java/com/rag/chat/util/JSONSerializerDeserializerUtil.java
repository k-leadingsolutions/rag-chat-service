package com.rag.chat.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Slf4j
public class JSONSerializerDeserializerUtil {

    public static JsonNode deserialize(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            String digest = sha256Short(json);
            log.debug("Failed to deserialize JSON (sha256_16={}): {}", digest, e.getMessage());
            return null;
        }
    }

    public static String serialize(Object obj, String fieldName) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("Failed to serialize {} field: {}", fieldName, e.getMessage());
            return null;
        }
    }

    private static String sha256Short(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 8); // first 8 bytes (16 hex chars)
        } catch (Exception e) {
            return "na";
        }
    }
}
