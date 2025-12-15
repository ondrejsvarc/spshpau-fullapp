package com.spshpau.projectservice.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NotConnectedException extends RuntimeException {
    public NotConnectedException(String message) {
        super(message);
    }
}
