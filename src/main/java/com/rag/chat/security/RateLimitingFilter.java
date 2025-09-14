package com.rag.chat.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter for service-to-service API calls.
 * Keys (API key or service JWT) or remote IP if unauthenticated.
 */
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Bandwidth bandwidth;

    public RateLimitingFilter(long capacity, long refillTokens, Duration refillPeriod) {
        this.bandwidth = Bandwidth.classic(capacity, Refill.intervally(refillTokens, refillPeriod));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/") || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    /**
     * Use the authenticated (API key or service JWT)
     * as the rate limiting key if available,
     * otherwise fall back to remote IP address.
     */
    private String resolveKey(HttpServletRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            return "auth:" + auth.getPrincipal();
        }
        return "ip:" + request.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String key = resolveKey(request);
        Bucket bucket = buckets.computeIfAbsent(key,
                k -> Bucket.builder().addLimit(bandwidth).build());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            long waitSeconds = (probe.getNanosToWaitForRefill() + 999_999_999) / 1_000_000_000;
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(waitSeconds));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.getWriter().write("""
                    {"status":429,"code":"RATE_LIMIT","message":"Rate limit exceeded"}""");
            return;
        }

        response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
        response.setHeader("X-RateLimit-Limit", String.valueOf(bandwidth.getCapacity()));
        chain.doFilter(request, response);
    }
}