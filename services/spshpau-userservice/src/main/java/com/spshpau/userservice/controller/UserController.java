package com.spshpau.userservice.controller;

import com.spshpau.userservice.dto.userdto.LocationUpdateRequest;
import com.spshpau.userservice.dto.userdto.UserDetailDto;
import com.spshpau.userservice.dto.userdto.UserSummaryDto;
import com.spshpau.userservice.model.User;
import com.spshpau.userservice.model.enums.ExperienceLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;


public interface UserController {

    /**
     * Retrieves the details of the currently authenticated user.
     * If the user's profile does not exist locally, it may attempt to sync it from Keycloak.
     *
     * @param jwt The JWT token representing the authenticated principal.
     * @return ResponseEntity containing the {@link UserDetailDto} if found or synced (200 OK),
     * or 401/404 if authentication fails or the user cannot be synced.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "current-user-uuid",
     * "username": "currentUser",
     * "email": "current.user@example.com",
     * "firstName": "Current",
     * "lastName": "User",
     * "location": "New York",
     * "active": true,
     * "artistProfile": {
     * "id": "current-user-uuid",
     * "availability": true,
     * "bio": "Artist bio",
     * "experienceLevel": "INTERMEDIATE",
     * "genres": [],
     * "skills": []
     * },
     * "producerProfile": null
     * }
     * }</pre>
     */
    ResponseEntity<UserDetailDto> getCurrentUser(Jwt jwt);

    /**
     * Retrieves a user by their username.
     *
     * @param username The username to search for.
     * @return ResponseEntity containing the {@link UserDetailDto} if found (200 OK),
     * or 404 Not Found if no user matches the username.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "some-user-uuid",
     * "username": "foundUser",
     * "email": "found.user@example.com",
     * "firstName": "Found",
     * "lastName": "User",
     * "location": "London",
     * "active": true,
     * "artistProfile": null,
     * "producerProfile": {
     * "id": "some-user-uuid",
     * "availability": false,
     * "bio": "Producer bio",
     * "experienceLevel": "EXPERT",
     * "genres": []
     * }
     * }
     * }</pre>
     */
    ResponseEntity<UserDetailDto> getUserByUsername(@RequestParam String username);

    /**
     * Retrieves a summary of a user by their unique ID.
     *
     * @param userId The UUID of the user to search for.
     * @return ResponseEntity containing the {@link UserSummaryDto} if found (200 OK),
     * or 404 Not Found if no user matches the ID.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "specific-user-uuid",
     * "username": "specificUser",
     * "firstName": "Specific",
     * "lastName": "User",
     * "location": "Berlin",
     * "artistProfile": {"availability": true, "experienceLevel": "BEGINNER"},
     * "producerProfile": null
     * }
     * }</pre>
     */
    ResponseEntity<UserSummaryDto> getUserById(UUID userId);

    /**
     * Updates the location for the currently authenticated user.
     *
     * @param jwt                 The JWT token representing the authenticated principal.
     * @param locationUpdateRequest DTO containing the new location information.
     * @return ResponseEntity containing the updated {@link UserDetailDto} (200 OK),
     * or an error status if the update fails (e.g., user not found, invalid location).
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "current-user-uuid",
     * "username": "currentUser",
     * "email": "current.user@example.com",
     * "firstName": "Current",
     * "lastName": "User",
     * "location": "Paris", // Updated location
     * "active": true,
     * "artistProfile": null,
     * "producerProfile": null
     * }
     * }</pre>
     */
    ResponseEntity<UserDetailDto> updateCurrentUserLocation(Jwt jwt, @RequestBody LocationUpdateRequest locationUpdateRequest);

    /**
     * Synchronizes the currently authenticated user's data from Keycloak
     * into the local application database. This process typically involves creating a new user record
     * if one doesn't exist or updating existing information (username, email, names).
     * User details are extracted from the provided JWT.
     *
     * @param jwt Represents the currently authenticated user (e.g., from Spring Security).
     * @return ResponseEntity containing the synchronized {@link UserDetailDto} (200 OK)
     * or an error status if synchronization fails (e.g., bad token, internal error).
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "keycloak-user-uuid",
     * "username": "keycloakUser",
     * "email": "keycloak.user@example.com",
     * "firstName": "Keycloak",
     * "lastName": "User",
     * "location": null,
     * "active": true,
     * "artistProfile": null,
     * "producerProfile": null
     * }
     * }</pre>
     */
    ResponseEntity<UserDetailDto> syncUserWithKeycloak(Jwt jwt);

    /**
     * Deactivates a user account, identified by their UUID.
     * This is an administrative action, requiring specific roles/permissions.
     *
     * @param userId The UUID of the user to deactivate.
     * @return ResponseEntity with 204 No Content if successful, or an error status.
     * Example Success Response (204 No Content):
     * (Empty Body)
     */
    ResponseEntity<Void> deactivateUser(@PathVariable UUID userId);

    /**
     * Reactivates a previously deactivated user account, identified by their UUID.
     * This is an administrative action, requiring specific roles/permissions.
     *
     * @param userId The UUID of the user to reactivate.
     * @return ResponseEntity with 204 No Content if successful, or an error status.
     * Example Success Response (204 No Content):
     * (Empty Body)
     */
    ResponseEntity<Void> reactivateUser(@PathVariable UUID userId);

    /**
     * Searches and filters active users based on a combination of criteria.
     * The current authenticated user is excluded from the search results.
     *
     * @param jwt                     The JWT token representing the authenticated principal (caller).
     * @param searchTerm              Optional string to match against username, first name, and last name.
     * @param genreIds                Optional list of Genre UUIDs to filter by (users must have at least one).
     * @param skillIds                Optional list of Skill UUIDs to filter by (users must have at least one in their artist profile).
     * @param hasArtist               Optional boolean to filter by the existence of an artist profile.
     * @param hasProducer             Optional boolean to filter by the existence of a producer profile.
     * @param artistExperienceLevel   Optional experience level to filter artists by.
     * @param artistAvailability      Optional boolean to filter artists by their availability.
     * @param producerExperienceLevel Optional experience level to filter producers by.
     * @param producerAvailability    Optional boolean to filter producers by their availability.
     * @param pageable                Pagination information (page number, size, sort).
     * @return A ResponseEntity containing a paginated list of {@link UserSummaryDto} objects
     * matching the criteria, or an error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "content": [
     * {
     * "id": "user1-uuid", "username": "userOne", "firstName": "User", "lastName": "One", "location": "City A",
     * "artistProfile": {"availability": true, "experienceLevel": "INTERMEDIATE"},
     * "producerProfile": null
     * },
     * {
     * "id": "user2-uuid", "username": "userTwo", "firstName": "User", "lastName": "Two", "location": "City B",
     * "artistProfile": null,
     * "producerProfile": {"availability": false, "experienceLevel": "EXPERT"}
     * }
     * ],
     * "pageable": {"offset": 0, "pageNumber": 0, "pageSize": 10, ...},
     * "totalPages": 1,
     * "totalElements": 2,
     * ...
     * }
     * }</pre>
     */
    ResponseEntity<Page<UserSummaryDto>> searchUsers(
            Jwt jwt,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) List<UUID> genreIds,
            @RequestParam(required = false) List<UUID> skillIds,
            @RequestParam(required = false) Boolean hasArtist,
            @RequestParam(required = false) Boolean hasProducer,
            @RequestParam(required = false) ExperienceLevel artistExperienceLevel,
            @RequestParam(required = false) Boolean artistAvailability,
            @RequestParam(required = false) ExperienceLevel producerExperienceLevel,
            @RequestParam(required = false) Boolean producerAvailability,
            Pageable pageable
    );

    /**
     * Finds potential collaborators for the currently authenticated user.
     * The matching logic is based on criteria such as shared genres, complementary profile types (artist/producer),
     * and availability. Results are ranked by relevance.
     *
     * @param jwt      The JWT token representing the authenticated principal.
     * @param pageable Pagination information.
     * @return A ResponseEntity containing a paginated list of matched {@link UserSummaryDto} objects,
     * ranked by relevance, or an error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "content": [
     * {
     * "id": "match1-uuid", "username": "matchOne", "firstName": "Match", "lastName": "One", "location": "City X",
     * "artistProfile": {"availability": true, "experienceLevel": "ADVANCED"},
     * "producerProfile": null
     * },
     * {
     * "id": "match2-uuid", "username": "matchTwo", "firstName": "Match", "lastName": "Two", "location": "City Y",
     * "artistProfile": null,
     * "producerProfile": {"availability": true, "experienceLevel": "INTERMEDIATE"}
     * }
     * ],
     * "pageable": {"offset": 0, "pageNumber": 0, "pageSize": 10, ...},
     * "totalPages": 2,
     * "totalElements": 15,
     * ...
     * }
     * }</pre>
     */
    ResponseEntity<Page<UserSummaryDto>> findMatches(Jwt jwt, Pageable pageable);
}
