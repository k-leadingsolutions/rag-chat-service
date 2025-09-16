package com.rag.chat.exception;

public class RateLimitExceededException extends RuntimeException {
    private final long retryAfterSeconds;

    public RateLimitExceededException(long retryAfterSeconds) {
        super("RATE_LIMIT");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
