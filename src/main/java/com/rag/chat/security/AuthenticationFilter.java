package com.rag.chat.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
public class AuthenticationFilter extends OncePerRequestFilter {


    private final JwtAuthenticationProvider jwtProvider;
    private final ApiKeyAuthenticationProvider apiKeyProvider;

    public AuthenticationFilter(JwtAuthenticationProvider jwtProvider, ApiKeyAuthenticationProvider apiKeyProvider) {
        this.jwtProvider = jwtProvider;
        this.apiKeyProvider = apiKeyProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (isPublicEndpoint(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String apiKey = request.getHeader(apiKeyProvider.getApiKeyHeaderName());

        Claims claims = null;
        boolean jwtValid = false;
        boolean apiKeyValid = false;

        // Validate JWT
        try {
            claims = jwtProvider.validate(authHeader);
            String audience = claims.getAudience();
            String service = claims.get("service", String.class);
            jwtValid = ("rag-service".equalsIgnoreCase(audience)) ||
                       ("rag".equalsIgnoreCase(audience)) ||
                       ("rag-service".equalsIgnoreCase(service)) ||
                       ("rag".equalsIgnoreCase(service));
        } catch (Exception ex) {
            log.warn("JWT validation failed: {} ", ex.getMessage());
        }

        // Validate API Key
        apiKeyValid = apiKeyProvider.isValid(apiKey);

        if (jwtValid && apiKeyValid) {
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        claims.getSubject(),
                    null,
                    List.of(
                        new SimpleGrantedAuthority("ROLE_API_CLIENT"),
                        new SimpleGrantedAuthority("ROLE_RAG_SERVICE")
                    ));
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.info("Authentication successful: valid JWT and valid API key.");
            chain.doFilter(request, response);
        } else {
            log.warn("Authentication failed. JWT valid: {}, API key valid: {}", jwtValid, apiKeyValid);
            throw new BadCredentialsException("Invalid JWT or API key");
        }
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/actuator/health");
    }
}