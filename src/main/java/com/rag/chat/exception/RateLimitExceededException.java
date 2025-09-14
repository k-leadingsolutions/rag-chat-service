package com.rag.chat.exception;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message){
        super(message);
    }
}