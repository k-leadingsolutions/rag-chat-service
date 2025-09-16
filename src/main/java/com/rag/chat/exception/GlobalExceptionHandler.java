package com.rag.chat.exception;

import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    private String msg(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        log.info("Translating key '{}' for locale '{}'", code, locale);
        return messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        String key = ex.getMessage();// Must be a valid translation key
        String translated = msg(key);
        log.warn("Resource not found: {}", key);
        return build(HttpStatus.NOT_FOUND, key, translated, req.getRequestURI());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest req) {
        String msg = msg("exception.no.handler") + ": " + ex.getMessage();
        log.warn("No Resource Found: {} ", msg);
        return build(HttpStatus.NOT_FOUND, "NO_RESOURCE_FOUND", msg, req.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("Bad request: {}", ex.getMessage());
        String key = ex.getMessage();
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", msg(key), req.getRequestURI());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest req) {
        log.warn("Entity not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", msg("session.not.found"), req.getRequestURI());
    }


    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiErrorResponse> handleValidation(Exception ex, HttpServletRequest req) {
        String errors = (ex instanceof MethodArgumentNotValidException manv)
                ? manv.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "))
                : ((BindException) ex).getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", errors);
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", errors, req.getRequestURI());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParams(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String msg = msg("exception.missing.parameter") + ": " + ex.getParameterName();
        log.warn(msg);
        return build(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", msg, req.getRequestURI());
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingPathVar(MissingPathVariableException ex, HttpServletRequest req) {
        String msg = msg("exception.missing.pathVariable") + ": " + ex.getVariableName();
        log.warn(msg);
        return build(HttpStatus.BAD_REQUEST, "MISSING_PATH_VARIABLE", msg, req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String msg = msg("exception.type.mismatch") + ": '" + ex.getName() + "' must be of type " + ex.getRequiredType().getSimpleName();
        log.warn(msg);
        return build(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", msg, req.getRequestURI());
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.warn("Malformed JSON: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_JSON", msg("exception.malformed.json"), req.getRequestURI());
    }


    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return Map.of(
                "status", 403,
                "code", "UNAUTHORIZED",
                "message", msg("auth.required")
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return Map.of(
                "status", 403,
                "code", "FORBIDDEN",
                "message",msg("auth.denied")
        );
    }

    @ExceptionHandler(JwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleJwtException(JwtException ex) {
        String key;
        switch (ex.getMessage()) {
            case "INVALID_HEADER" -> key = "auth.header.invalid";
            case "EMPTY_TOKEN" -> key = "auth.token.empty";
            default -> key = "auth.unknown";
        }
        log.warn("JWT error: {}", key);
        return Map.of(
                "status", 401,
                "code", key,
                "message", msg(key)
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Map<String, Object> handleRateLimitExceeded(RateLimitExceededException ex) {
        log.warn("Rate limit exceeded, retry after {} seconds", ex.getRetryAfterSeconds());
        return Map.of(
                "status", 429,
                "code", "RATE_LIMIT",
                "message", msg("rate.limit.exceeded"),
                "retryAfterSeconds", ex.getRetryAfterSeconds()
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        log.warn("Method not allowed: {}", ex.getMethod());
        return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest req) {
        log.warn("Unsupported media type: {}", ex.getContentType());
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiErrorResponse> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpServletRequest req) {
        log.warn("Media type not acceptable: {}", ex.getMessage());
        return build(HttpStatus.NOT_ACCEPTABLE, "NOT_ACCEPTABLE", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest req) {
        log.warn("No handler found for request: {}", req.getRequestURI());
        return build(HttpStatus.NOT_FOUND, "NO_HANDLER_FOUND", msg("exception.no.handler"), req.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION", msg("exception.data.integrity"), req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error at path: {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", msg("exception.unexpected"), req.getRequestURI());
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String code, String msg, String path) {
        return ResponseEntity.status(status)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .code(code)
                        .message(msg)
                        .path(path)
                        .build());
    }
}
