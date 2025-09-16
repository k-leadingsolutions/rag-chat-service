package com.rag.chat.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class CorrelationIdFilter implements Filter {
    /**
     * API filter to add correlation-IDS
     * for tracing and logging
     */
    public static final String HEADER = "X-Request-Id";

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest http = (HttpServletRequest) request;
        String id = http.getHeader(HEADER);
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString().trim();
            log.debug("Generated new correlation ID: {}", id);
        }

        MDC.put("requestId", id);
        ((HttpServletResponse) response).setHeader(HEADER, id);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("requestId");
        }
    }
}
