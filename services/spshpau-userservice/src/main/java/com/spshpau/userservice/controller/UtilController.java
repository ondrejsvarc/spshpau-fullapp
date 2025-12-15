package com.spshpau.userservice.controller;


import org.springframework.http.ResponseEntity;

public interface UtilController {
    /**
     * A simple health check or ping endpoint.
     * Returns a static response (e.g., "Pong!") to indicate the service is running.
     * This endpoint is usually publicly accessible.
     *
     * @return ResponseEntity containing a String response (e.g., "Pong!").
     * Example Success Response (200 OK):
     * <pre>{@code
     * "Pong!"
     * }</pre>
     */
    ResponseEntity<String> ping();

    /**
     * An endpoint to test if a provided authentication token is valid.
     * If the request reaches this endpoint and is processed successfully (after passing
     * through security filters), it implies the token was accepted.
     *
     * @return ResponseEntity containing a String message confirming successful authentication.
     * Example Success Response (200 OK):
     * <pre>{@code
     * "You have sent a valid authentication token!"
     * }</pre>
     */
    ResponseEntity<String> auth();
}
