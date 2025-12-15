package com.spshpau.projectservice.controller;


import org.springframework.http.ResponseEntity;

public interface UtilController {
    /**
     * A simple ping endpoint to check service availability.
     *
     * @return A ResponseEntity containing the string "Pong!" and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * Pong!
     * }</pre>
     */
    ResponseEntity<String> ping();

    /**
     * An endpoint to verify if the provided authentication token is valid.
     * This endpoint would typically be secured.
     *
     * @return A ResponseEntity containing a success message if authentication is valid, and HTTP status.
     * Example Response (200 OK):
     * <pre>{@code
     * You have sent a valid authentication token!
     * }</pre>
     */
    ResponseEntity<String> auth();
}