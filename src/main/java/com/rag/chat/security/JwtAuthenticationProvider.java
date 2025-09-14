package com.rag.chat.security;

import com.rag.chat.util.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.util.StringUtils;

public class JwtAuthenticationProvider {

    private final JWTUtil jwtUtil;

    public JwtAuthenticationProvider(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public Claims validate(String authHeader) throws JwtException {
        if (!StringUtils.hasText(authHeader) || !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new JwtException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new JwtException("Empty Bearer token");
        }
        return jwtUtil.validateToken(token).getBody();
    }
}