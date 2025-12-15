package com.spshpau.userservice.controller;

import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.dto.profiledto.ProducerProfileDetailDto;
import com.spshpau.userservice.dto.profiledto.ProfileUpdateDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Set;
import java.util.UUID;

public interface ProducerProfileController {
    /**
     * Retrieves the Producer Profile for the currently authenticated user.
     *
     * @param jwt The JWT token of the authenticated user.
     * @return ResponseEntity containing the {@link ProducerProfileDetailDto} if found,
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "user-uuid-for-producer",
     * "availability": true,
     * "bio": "Producer specializing in electronic music.",
     * "experienceLevel": "ADVANCED",
     * "genres": [
     * {"id": "genre-uuid-5", "name": "Techno"},
     * {"id": "genre-uuid-6", "name": "House"}
     * ]
     * }
     * }</pre>
     */
    ResponseEntity<ProducerProfileDetailDto> getMyProducerProfile(Jwt jwt);

    /**
     * Creates a new Producer Profile or fully updates an existing one
     * for the currently authenticated user.
     *
     * @param jwt         The JWT token of the authenticated user.
     * @param profileData DTO containing the data for creating or updating the profile.
     * @return ResponseEntity containing the created or updated {@link ProducerProfileDetailDto},
     * otherwise an appropriate error status.
     * Example Success Response (200 OK or 201 Created):
     * <pre>{@code
     * {
     * "id": "user-uuid-for-producer",
     * "availability": true,
     * "bio": "Updated bio for producer.",
     * "experienceLevel": "EXPERT",
     * "genres": []
     * }
     * }</pre>
     */
    ResponseEntity<ProducerProfileDetailDto> createOrUpdateMyProducerProfile(Jwt jwt, @RequestBody ProfileUpdateDto profileData);

    /**
     * Partially updates the Producer Profile for the currently authenticated user.
     * Only the fields present in the request body will be updated.
     *
     * @param jwt         The JWT token of the authenticated user.
     * @param profileData DTO containing the fields to update.
     * @return ResponseEntity containing the updated {@link ProducerProfileDetailDto},
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "user-uuid-for-producer",
     * "availability": false,
     * "bio": "Producer specializing in electronic music. Currently unavailable.",
     * "experienceLevel": "ADVANCED",
     * "genres": [
     * {"id": "genre-uuid-5", "name": "Techno"}
     * ]
     * }
     * }</pre>
     */
    ResponseEntity<ProducerProfileDetailDto> patchMyProducerProfile(Jwt jwt, @RequestBody ProfileUpdateDto profileData);

    /**
     * Retrieves the set of genres associated with the current user's Producer Profile.
     *
     * @param jwt The JWT token of the authenticated user.
     * @return ResponseEntity containing a Set of {@link GenreSummaryDto} for the user's producer profile,
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * [
     * {"id": "genre-uuid-5", "name": "Techno"},
     * {"id": "genre-uuid-6", "name": "House"}
     * ]
     * }</pre>
     */
    ResponseEntity<Set<GenreSummaryDto>> getMyProducerProfileGenres(Jwt jwt);

    /**
     * Adds a pre-existing Genre to the current user's Producer Profile.
     *
     * @param jwt     The JWT token of the authenticated user.
     * @param genreId The UUID of the Genre to add.
     * @return ResponseEntity containing the updated {@link ProducerProfileDetailDto} with the new genre,
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "user-uuid-for-producer",
     * "availability": true,
     * "bio": "Bio",
     * "experienceLevel": "ADVANCED",
     * "genres": [
     * {"id": "genre-uuid-5", "name": "Techno"},
     * {"id": "newly-added-genre-uuid-producer", "name": "Ambient"}
     * ]
     * }
     * }</pre>
     */
    ResponseEntity<ProducerProfileDetailDto> addGenreToMyProducerProfile(Jwt jwt, @PathVariable UUID genreId);

    /**
     * Removes a Genre association from the current user's Producer Profile.
     *
     * @param jwt     The JWT token of the authenticated user.
     * @param genreId The UUID of the Genre to remove.
     * @return ResponseEntity containing the updated {@link ProducerProfileDetailDto} without the specified genre,
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "user-uuid-for-producer",
     * "availability": true,
     * "bio": "Bio",
     * "experienceLevel": "ADVANCED",
     * "genres": [
     * {"id": "genre-uuid-5", "name": "Techno"}
     * ]
     * }
     * }</pre>
     */
    ResponseEntity<ProducerProfileDetailDto> removeGenreFromMyProducerProfile(Jwt jwt, @PathVariable UUID genreId);

    /**
     * Retrieves a publicly accessible Producer Profile by the user's username.
     *
     * @param username The username of the user whose producer profile is to be retrieved.
     * @return ResponseEntity containing the {@link ProducerProfileDetailDto} if found,
     * otherwise a 404 Not Found status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "another-user-uuid",
     * "availability": false,
     * "bio": "Public bio of the producer.",
     * "experienceLevel": "BEGINNER",
     * "genres": [
     * {"id": "genre-uuid-7", "name": "Hip Hop"}
     * ]
     * }
     * }</pre>
     */
    ResponseEntity<ProducerProfileDetailDto> getProducerProfileByUsername(@PathVariable String username);

    /**
     * Retrieves the set of genres for a publicly accessible Producer Profile by username.
     *
     * @param username The username of the user whose producer profile genres are to be retrieved.
     * @return ResponseEntity containing a Set of {@link GenreSummaryDto} if the profile is found,
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * [
     * {"id": "genre-uuid-7", "name": "Hip Hop"},
     * {"id": "genre-uuid-8", "name": "Funk"}
     * ]
     * }</pre>
     */
    ResponseEntity<Set<GenreSummaryDto>> getProducerProfileGenresByUsername(@PathVariable String username);
}
