package com.spshpau.userservice.controller.impl;

import com.spshpau.userservice.controller.UserController;
import com.spshpau.userservice.dto.userdto.LocationUpdateRequest;
import com.spshpau.userservice.dto.userdto.UserDetailDto;
import com.spshpau.userservice.dto.userdto.UserSearchCriteria;
import com.spshpau.userservice.dto.userdto.UserSummaryDto;
import com.spshpau.userservice.model.User;
import com.spshpau.userservice.model.enums.ExperienceLevel;
import com.spshpau.userservice.services.UserService;
import com.spshpau.userservice.services.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserControllerImpl implements UserController {

    private final UserService userService;

    @Autowired
    public UserControllerImpl(UserService userService) {
        this.userService = userService;
    }

    // Helper method to extract UUID from JWT
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

    @Override
    @GetMapping("/me")
    public ResponseEntity<UserDetailDto> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        UUID userUuid = getUserIdFromJwt(jwt);
        return userService.getUserDetailById(userUuid)
                .map(user -> {
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    // User doesn't have profile yet
                    return syncUserWithKeycloak(jwt);
                });
    }

    @Override
    @GetMapping("/search/username/{username}")
    public ResponseEntity<UserDetailDto> getUserByUsername(@PathVariable String username) {
        return userService.getUserDetailByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    @GetMapping("/search/id/{userId}")
    public ResponseEntity<UserSummaryDto> getUserById(@PathVariable UUID userId) {
        User user = userService.getUserEntityById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        UserSummaryDto dto = new UserSummaryDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setLocation(user.getLocation());

        return ResponseEntity.ok(dto);
    }

    @Override
    @PutMapping("/me/location")
    public ResponseEntity<UserDetailDto> updateCurrentUserLocation(@AuthenticationPrincipal Jwt jwt,
                                                          @RequestBody LocationUpdateRequest locationUpdateRequest) {
        UUID userUuid = getUserIdFromJwt(jwt);
        String newLocation = locationUpdateRequest.getLocation();

        // Basic validation for location
        if (newLocation == null || newLocation.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            UserDetailDto user = userService.updateUserLocation(userUuid, newLocation);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found", ex);
        }
    }

    @Override
    @PutMapping("/me/sync")
    public ResponseEntity<UserDetailDto> syncUserWithKeycloak(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // --- Extract details from the authenticated user (JWT token) ---
        String keycloakId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");

        // Basic validation
        if (keycloakId == null || username == null || email == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        UUID keycloakUuid;
        try {
            // *** Convert the String ID from JWT subject to UUID ***
            keycloakUuid = UUID.fromString(keycloakId);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid UUID format received from Keycloak token subject: " + keycloakId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            UserDetailDto syncedUser = userService.syncUserFromKeycloak(keycloakUuid, username, email, firstName, lastName);
            return ResponseEntity.ok(syncedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID userId) {
        try {
            userService.deactivateUser(userId);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deactivating user", ex);
        }
    }

    @PutMapping("/{userId}/reactivate")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Void> reactivateUser(@PathVariable UUID userId) {
        try {
            userService.reactivateUser(userId);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reactivating user", ex);
        }
    }

    @Override
    @GetMapping("/search/filter")
    public ResponseEntity<Page<UserSummaryDto>> searchUsers(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) List<UUID> genreIds,
            @RequestParam(required = false) List<UUID> skillIds,
            @RequestParam(required = false) Boolean hasArtist,
            @RequestParam(required = false) Boolean hasProducer,
            @RequestParam(required = false) ExperienceLevel artistExperienceLevel,
            @RequestParam(required = false) Boolean artistAvailability,
            @RequestParam(required = false) ExperienceLevel producerExperienceLevel,
            @RequestParam(required = false) Boolean producerAvailability,
            @PageableDefault(size = 10, sort = "username") Pageable pageable) {

        UUID currentUserId = getUserIdFromJwt(jwt);

        UserSearchCriteria criteria = new UserSearchCriteria();
        criteria.setSearchTerm(searchTerm);
        criteria.setGenreIds(genreIds);
        criteria.setSkillIds(skillIds);
        criteria.setHasArtistProfile(hasArtist);
        criteria.setHasProducerProfile(hasProducer);
        criteria.setArtistExperienceLevel(artistExperienceLevel);
        criteria.setArtistAvailability(artistAvailability);
        criteria.setProducerExperienceLevel(producerExperienceLevel);
        criteria.setProducerAvailability(producerAvailability);

        try {
            Page<UserSummaryDto> results = userService.findActiveUsers(currentUserId, criteria, pageable);
            return ResponseEntity.ok(results);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error searching users", ex);
        }
    }

    @Override
    @GetMapping("/matches")
    public ResponseEntity<Page<UserSummaryDto>> findMatches(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 10) Pageable pageable) {

        UUID currentUserId = getUserIdFromJwt(jwt);

        try {
            Page<UserSummaryDto> results = userService.findMatches(currentUserId, pageable);
            return ResponseEntity.ok(results);
        } catch (UserNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error finding matches", ex);
        }
    }
}
