package com.spshpau.userservice.services.exceptions;

public class GenreLimitExceededException extends RuntimeException {
    public GenreLimitExceededException(String message) {
        super(message);
    }
}


