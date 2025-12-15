package com.spshpau.userservice.controller.impl;

import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.dto.profiledto.ProducerProfileDetailDto;
import com.spshpau.userservice.dto.profiledto.ProfileUpdateDto;
import com.spshpau.userservice.services.exceptions.GenreLimitExceededException;
import com.spshpau.userservice.services.exceptions.GenreNotFoundException;
import com.spshpau.userservice.services.exceptions.UserNotFoundException;
import com.spshpau.userservice.services.exceptions.ProfileNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.spshpau.userservice.controller.ProducerProfileController;
import com.spshpau.userservice.services.ProducerProfileService;
import com.spshpau.userservice.model.*;

import java.util.Set;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/users/producer-profile")
@RequiredArgsConstructor
public class ProducerProfileControllerImpl implements ProducerProfileController {
    private final ProducerProfileService producerProfileService;

    // --- Helper Method ---
    private UUID getUserIdFromJwt(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication token is missing");
        }
        String subject = jwt.getSubject();
        if (subject == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token missing subject claim");
        }
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user identifier in token");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ProducerProfileDetailDto> getMyProducerProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = getUserIdFromJwt(jwt);
        return producerProfileService.getProducerProfileByUserId(userId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producer profile not found for current user"));
    }

    @PutMapping("/me/create")
    public ResponseEntity<ProducerProfileDetailDto> createOrUpdateMyProducerProfile(@AuthenticationPrincipal Jwt jwt,
                                                                           @RequestBody ProfileUpdateDto profileData) {
        UUID userId = getUserIdFromJwt(jwt);
        try {
            ProducerProfileDetailDto profile = producerProfileService.createOrUpdateProducerProfile(userId, profileData);
            return ResponseEntity.ok(profile);
        } catch (UserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found, cannot create/update profile", ex);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saving producer profile", ex);
        }
    }

    @PatchMapping("/me/patch")
    public ResponseEntity<ProducerProfileDetailDto> patchMyProducerProfile(@AuthenticationPrincipal Jwt jwt,
                                                                  @RequestBody ProfileUpdateDto profileData) {
        UUID userId = getUserIdFromJwt(jwt);
        try {
            ProducerProfileDetailDto profile = producerProfileService.patchProducerProfile(userId, profileData);
            return ResponseEntity.ok(profile);
        } catch (ProfileNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error patching producer profile", ex);
        }
    }

    @GetMapping("/me/genres")
    public ResponseEntity<Set<GenreSummaryDto>> getMyProducerProfileGenres(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = getUserIdFromJwt(jwt);
        try {
            Set<GenreSummaryDto> genres = producerProfileService.getProducerProfileGenres(userId);
            return ResponseEntity.ok(genres);
        } catch (ProfileNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PostMapping("/me/genres/add/{genreId}")
    public ResponseEntity<ProducerProfileDetailDto> addGenreToMyProducerProfile(@AuthenticationPrincipal Jwt jwt,
                                                                       @PathVariable UUID genreId) {
        UUID userId = getUserIdFromJwt(jwt);

        try {
            ProducerProfileDetailDto updatedProfile = producerProfileService.addGenreToProducerProfile(userId, genreId);
            return ResponseEntity.ok(updatedProfile);
        } catch (ProfileNotFoundException | GenreNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (GenreLimitExceededException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error adding genre", ex);
        }
    }

    @DeleteMapping("/me/genres/remove/{genreId}")
    public ResponseEntity<ProducerProfileDetailDto> removeGenreFromMyProducerProfile(@AuthenticationPrincipal Jwt jwt,
                                                                            @PathVariable UUID genreId) {
        UUID userId = getUserIdFromJwt(jwt);
        try {
            ProducerProfileDetailDto updatedProfile = producerProfileService.removeGenreFromProducerProfile(userId, genreId);
            return ResponseEntity.ok(updatedProfile);
        } catch (ProfileNotFoundException | GenreNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error removing genre", ex);
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<ProducerProfileDetailDto> getProducerProfileByUsername(@PathVariable String username) {
        return producerProfileService.getProducerProfileByUsername(username)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Producer profile not found for username: " + username);
                });
    }
    @GetMapping("/{username}/genres")
    public ResponseEntity<Set<GenreSummaryDto>> getProducerProfileGenresByUsername(@PathVariable String username) {
        try {
            Set<GenreSummaryDto> genres = producerProfileService.getProducerProfileGenresByUsername(username);
            return ResponseEntity.ok(genres);
        } catch (UserNotFoundException | ProfileNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producer profile or user not found for username: " + username, ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving genres", ex);
        }
    }
}
