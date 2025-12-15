package com.spshpau.projectservice.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CollaboratorAlreadyExistsException extends RuntimeException {
    public CollaboratorAlreadyExistsException(String message) {
        super(message);
    }
}
