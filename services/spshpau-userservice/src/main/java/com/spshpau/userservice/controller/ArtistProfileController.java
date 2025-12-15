package com.spshpau.userservice.controller;

import com.spshpau.userservice.dto.profiledto.ArtistProfileDetailDto;
import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.dto.profiledto.ProfileUpdateDto;
import com.spshpau.userservice.dto.profiledto.SkillSummaryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


import java.util.Set;
import java.util.UUID;

public interface ArtistProfileController {

    /**
     * Retrieves the Artist Profile for the currently authenticated user.
     *
     * @param jwt The JWT token of the authenticated user.
     * @return ResponseEntity containing the {@link ArtistProfileDetailDto} if found,
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "123e4567-e89b-12d3-a456-426614174000",
     * "availability": true,
     * "bio": "Experienced session guitarist and songwriter.",
     * "experienceLevel": "EXPERT",
     * "genres": [
     * {"id": "genre-uuid-1", "name": "Rock"},
     * {"id": "genre-uuid-2", "name": "Blues"}
     * ],
     * "skills": [
     * {"id": "skill-uuid-1", "name": "Guitar"},
     * {"id": "skill-uuid-2", "name": "Songwriting"}
     * ]
     * }
     * }</pre>
     */
    ResponseEntity<ArtistProfileDetailDto> getMyArtistProfile(Jwt jwt);

    /**
     * Creates a new Artist Profile or fully updates an existing one
     * for the currently authenticated user.
     *
     * @param jwt         The JWT token of the authenticated user.
     * @param profileData DTO containing the data for creating or updating the profile.
     * @return ResponseEntity containing the created or updated {@link ArtistProfileDetailDto},
     * otherwise an appropriate error status.
     * Example Success Response (200 OK or 201 Created):
     * <pre>{@code
     * {
     * "id": "123e4567-e89b-12d3-a456-426614174000",
     * "availability": false,
     * "bio": "New bio for the artist.",
     * "experienceLevel": "INTERMEDIATE",
     * "genres": [],
     * "skills": []
     * }
     * }</pre>
     */
    ResponseEntity<ArtistProfileDetailDto> createOrUpdateMyArtistProfile(Jwt jwt, @RequestBody ProfileUpdateDto profileData);

    /**
     * Partially updates the Artist Profile for the currently authenticated user.
     * Only the fields present in the request body will be updated.
     *
     * @param jwt         The JWT token of the authenticated user.
     * @param profileData DTO containing the fields to update.
     * @return ResponseEntity containing the updated {@link ArtistProfileDetailDto},
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "123e4567-e89b-12d3-a456-426614174000",
     * "availability": true,
     * "bio": "Updated bio for the artist.",
     * "experienceLevel": "ADVANCED",
     * "genres": [
     * {"id": "genre-uuid-1", "name": "Rock"}
     * ],
     * "skills": [
     * {"id": "skill-uuid-1", "name": "Guitar"}
     * ]
     * }
     * }</pre>
     */
    ResponseEntity<ArtistProfileDetailDto> patchMyArtistProfile(Jwt jwt, @RequestBody ProfileUpdateDto profileData);

    /**
     * Retrieves the set of genres associated with the current user's Artist Profile.
     *
     * @param jwt The JWT token of the authenticated user.
     * @return ResponseEntity containing a Set of {@link GenreSummaryDto} for the user's artist profile,
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * [
     * {"id": "genre-uuid-1", "name": "Rock"},
     * {"id": "genre-uuid-2", "name": "Pop"}
     * ]
     * }</pre>
     */
    ResponseEntity<Set<GenreSummaryDto>> getMyArtistProfileGenres(Jwt jwt);

    /**
     * Adds a pre-existing Genre to the current user's Artist Profile.
     *
     * @param jwt     The JWT token of the authenticated user.
     * @param genreId The UUID of the Genre to add.
     * @return ResponseEntity containing the updated {@link ArtistProfileDetailDto} with the new genre,
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "123e4567-e89b-12d3-a456-426614174000",
     * "availability": true,
     * "bio": "Bio",
     * "experienceLevel": "EXPERT",
     * "genres": [
     * {"id": "genre-uuid-1", "name": "Rock"},
     * {"id": "newly-added-genre-uuid", "name": "Funk"}
     * ],
     * "skills": []
     * }
     * }</pre>
     */
    ResponseEntity<ArtistProfileDetailDto> addGenreToMyArtistProfile(Jwt jwt, @PathVariable UUID genreId);

    /**
     * Removes a Genre association from the current user's Artist Profile.
     *
     * @param jwt     The JWT token of the authenticated user.
     * @param genreId The UUID of the Genre to remove.
     * @return ResponseEntity containing the updated {@link ArtistProfileDetailDto} without the specified genre,
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "123e4567-e89b-12d3-a456-426614174000",
     * "availability": true,
     * "bio": "Bio",
     * "experienceLevel": "EXPERT",
     * "genres": [
     * {"id": "genre-uuid-1", "name": "Rock"}
     * ],
     * "skills": []
     * }
     * }</pre>
     */
    ResponseEntity<ArtistProfileDetailDto> removeGenreFromMyArtistProfile(Jwt jwt, @PathVariable UUID genreId);

    /**
     * Retrieves the set of skills associated with the current user's Artist Profile.
     *
     * @param jwt The JWT token of the authenticated user.
     * @return ResponseEntity containing a Set of {@link SkillSummaryDto} for the user's artist profile,
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * [
     * {"id": "skill-uuid-1", "name": "Guitar"},
     * {"id": "skill-uuid-2", "name": "Vocals"}
     * ]
     * }</pre>
     */
    ResponseEntity<Set<SkillSummaryDto>> getMyArtistProfileSkills(Jwt jwt);

    /**
     * Adds a pre-existing Skill to the current user's Artist Profile.
     *
     * @param jwt     The JWT token of the authenticated user.
     * @param skillId The UUID of the Skill to add.
     * @return ResponseEntity containing the updated {@link ArtistProfileDetailDto} with the new skill,
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "123e4567-e89b-12d3-a456-426614174000",
     * "availability": true,
     * "bio": "Bio",
     * "experienceLevel": "EXPERT",
     * "genres": [],
     * "skills": [
     * {"id": "skill-uuid-1", "name": "Guitar"},
     * {"id": "newly-added-skill-uuid", "name": "Piano"}
     * ]
     * }
     * }</pre>
     */
    ResponseEntity<ArtistProfileDetailDto> addSkillToMyArtistProfile(Jwt jwt, @PathVariable UUID skillId);

    /**
     * Removes a Skill association from the current user's Artist Profile.
     *
     * @param jwt     The JWT token of the authenticated user.
     * @param skillId The UUID of the Skill to remove.
     * @return ResponseEntity containing the updated {@link ArtistProfileDetailDto} without the specified skill,
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "123e4567-e89b-12d3-a456-426614174000",
     * "availability": true,
     * "bio": "Bio",
     * "experienceLevel": "EXPERT",
     * "genres": [],
     * "skills": [
     * {"id": "skill-uuid-1", "name": "Guitar"}
     * ]
     * }
     * }</pre>
     */
    ResponseEntity<ArtistProfileDetailDto> removeSkillFromMyArtistProfile(Jwt jwt, @PathVariable UUID skillId);

    /**
     * Retrieves a publicly accessible Artist Profile by the user's username.
     *
     * @param username The username of the user whose artist profile is to be retrieved.
     * @return ResponseEntity containing the {@link ArtistProfileDetailDto} if found,
     * otherwise a 404 Not Found status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "id": "some-user-uuid",
     * "availability": true,
     * "bio": "Public bio of the artist.",
     * "experienceLevel": "INTERMEDIATE",
     * "genres": [
     * {"id": "genre-uuid-3", "name": "Electronic"}
     * ],
     * "skills": [
     * {"id": "skill-uuid-3", "name": "DJing"}
     * ]
     * }
     * }</pre>
     */
    ResponseEntity<ArtistProfileDetailDto> getArtistProfileByUsername(@PathVariable String username);

    /**
     * Retrieves the set of genres for a publicly accessible Artist Profile by username.
     *
     * @param username The username of the user whose artist profile genres are to be retrieved.
     * @return ResponseEntity containing a Set of {@link GenreSummaryDto} if the profile is found,
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * [
     * {"id": "genre-uuid-3", "name": "Electronic"},
     * {"id": "genre-uuid-4", "name": "Ambient"}
     * ]
     * }</pre>
     */
    ResponseEntity<Set<GenreSummaryDto>> getArtistProfileGenresByUsername(@PathVariable String username);

    /**
     * Retrieves the set of skills for a publicly accessible Artist Profile by username.
     *
     * @param username The username of the user whose artist profile skills are to be retrieved.
     * @return ResponseEntity containing a Set of {@link SkillSummaryDto} if the profile is found,
     * otherwise an appropriate error status.
     * Example Success Response (200 OK):
     * <pre>{@code
     * [
     * {"id": "skill-uuid-3", "name": "DJing"},
     * {"id": "skill-uuid-4", "name": "Music Production"}
     * ]
     * }</pre>
     */
    ResponseEntity<Set<SkillSummaryDto>> getArtistProfileSkillsByUsername(@PathVariable String username);
}
