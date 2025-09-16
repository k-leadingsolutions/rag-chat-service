package com.rag.chat.security;

import com.rag.chat.util.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class JwtAuthenticationProvider {

    private final JWTUtil jwtUtil;

    /**
     * Validate JWT
     * @param authHeader
     * @return
     * @throws JwtException
     */
    public Claims validate(String authHeader) throws JwtException {
        if (!StringUtils.hasText(authHeader) || !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new JwtException("INVALID HEADER");
        }
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new JwtException("INVALID TOKEN");
        }
        return jwtUtil.validateToken(token).getBody();
    }
}