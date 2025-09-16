package com.rag.chat.config;

import com.rag.chat.security.ApiKeyAuthenticationProvider;
import com.rag.chat.security.AuthenticationFilter;
import com.rag.chat.security.JwtAuthenticationProvider;
import com.rag.chat.security.RateLimitingFilter;
import com.rag.chat.util.JWTUtil;
import com.rag.chat.util.Translator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

    @Value("${app.security.api-keys}")
    private String apiKeysProperty;

    @Value("${app.ratelimit.capacity}")
    private long rateLimitCapacity;

    @Value("${app.ratelimit.refillTokens}")
    private long rateLimitRefillTokens;

    @Value("${app.ratelimit.refillPeriod}")
    private String rateLimitRefillPeriod; // Will parse to Duration

    /**
     * JwtAuthenticationProvider Filter
     * @return
     */

    private final Translator translator;

    public SecurityConfig(Translator translator) {
        this.translator = translator;
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(JWTUtil jwtUtil) {
        return new JwtAuthenticationProvider(jwtUtil);
    }

    /**
     * ApiKeyAuthentication Filter
     * @return
     */
    @Bean
    public ApiKeyAuthenticationProvider apiKeyAuthenticationProvider() {
        Set<String> apiKeys = Arrays.stream(apiKeysProperty.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        return new ApiKeyAuthenticationProvider(apiKeys, "x-api-key");
    }

    /**
     * Authentication strategy for JWT And ApiKey authentication
     * @param jwtProvider
     * @param apiKeyProvider
     * @return
     */
    @Bean
    public AuthenticationFilter authenticationFilter(
            JwtAuthenticationProvider jwtProvider,
            ApiKeyAuthenticationProvider apiKeyProvider) {
        return new AuthenticationFilter(jwtProvider, apiKeyProvider);
    }

    /**
     * Rate Limiter Filtering step
     * @return
     */

    @Bean
    public RateLimitingFilter rateLimitingFilter() {
        return new RateLimitingFilter(rateLimitCapacity, rateLimitRefillTokens, Duration.parse(rateLimitRefillPeriod));
    }

    /**
     * Security Config layer, including filtering steps for service-to-service use case
     * @param http
     * @param authenticationFilter
     * @param rateLimitingFilter
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationFilter authenticationFilter,
            RateLimitingFilter rateLimitingFilter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/**").hasAnyAuthority("ROLE_API_CLIENT", "ROLE_RAG_SERVICE")
                        .anyRequest().denyAll()
                )   .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            sendLocalizedError(res, req, 401, "UNAUTHORIZED", "auth.required");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            sendLocalizedError(res, req, 403, "FORBIDDEN", "auth.denied");
                        }))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                );

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(rateLimitingFilter, AuthenticationFilter.class);

        return http.build();
    }

    private void sendLocalizedError(HttpServletResponse res, HttpServletRequest req, int status, String code, String msgKey) throws IOException {
        Locale locale = Translator.getLocaleFromRequest(req);
        String msg = translator.toLocale(msgKey, locale);
        res.setStatus(status);
        res.setContentType("application/json; charset=UTF-8");
        res.getWriter().write(
                String.format("{\"status\":%d,\"code\":\"%s\",\"message\":\"%s\"}", status, code, msg)
        );
    }

}