package com.spshpau.projectservice.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN) // Or BAD_REQUEST depending on context
public class UserNotProjectMemberException extends RuntimeException {
    public UserNotProjectMemberException(String message) {
        super(message);
    }
}
