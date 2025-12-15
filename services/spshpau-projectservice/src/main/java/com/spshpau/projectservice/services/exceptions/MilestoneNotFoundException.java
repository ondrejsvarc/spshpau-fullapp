package com.spshpau.projectservice.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MilestoneNotFoundException extends RuntimeException {
    public MilestoneNotFoundException(String message) {
        super(message);
    }
}
