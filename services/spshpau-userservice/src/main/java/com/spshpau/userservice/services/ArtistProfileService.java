package com.spshpau.userservice.services;

import com.spshpau.userservice.dto.profiledto.ArtistProfileDetailDto;
import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.dto.profiledto.ProfileUpdateDto;
import com.spshpau.userservice.dto.profiledto.SkillSummaryDto;
import com.spshpau.userservice.model.ArtistProfile;
import com.spshpau.userservice.model.Genre;
import com.spshpau.userservice.model.Skill;


import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ArtistProfileService {

    /**
     * Retrieves the detailed artist profile for a given user ID.
     *
     * @param userId The unique identifier of the user whose artist profile is to be retrieved.
     * @return An {@link Optional} containing the {@link ArtistProfileDetailDto} if found,
     * otherwise an empty Optional.
     */
    Optional<ArtistProfileDetailDto> getArtistProfileByUserId(UUID userId);

    /**
     * Creates a new artist profile or fully updates an existing one for the specified user.
     * If a profile for the user ID already exists, it will be updated with the provided data.
     * If not, a new profile will be created.
     *
     * @param userId       The unique identifier of the user for whom the profile is being created or updated.
     * @param profileData  A {@link ProfileUpdateDto} containing the data to create or update the profile.
     * Must not be null. ExperienceLevel is required for new profile creation.
     * @return The created or updated {@link ArtistProfileDetailDto}.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if the user with the given ID does not exist.
     * @throws IllegalArgumentException if experienceLevel is null during new profile creation.
     */
    ArtistProfileDetailDto createOrUpdateArtistProfile(UUID userId, ProfileUpdateDto profileData);

    /**
     * Partially updates an existing artist profile for the specified user.
     * Only the non-null fields in the {@code profileUpdateDto} will be applied to the existing profile.
     *
     * @param userId            The unique identifier of the user whose artist profile is to be patched.
     * @param profileUpdateDto  A {@link ProfileUpdateDto} containing the fields to update. Must not be null.
     * @return The updated {@link ArtistProfileDetailDto}.
     * @throws com.spshpau.userservice.services.exceptions.ProfileNotFoundException if no artist profile exists for the given user ID.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if the user with the given ID does not exist (checked if profile needs user context).
     */
    ArtistProfileDetailDto patchArtistProfile(UUID userId, ProfileUpdateDto profileUpdateDto);

    /**
     * Adds a specified genre to an existing artist profile.
     *
     * @param userId  The unique identifier of the user whose artist profile will be modified.
     * @param genreId The unique identifier of the genre to add.
     * @return The updated {@link ArtistProfileDetailDto} reflecting the added genre.
     * @throws com.spshpau.userservice.services.exceptions.ProfileNotFoundException if the artist profile does not exist.
     * @throws com.spshpau.userservice.services.exceptions.GenreNotFoundException if the genre with the given ID does not exist.
     * @throws com.spshpau.userservice.services.exceptions.GenreLimitExceededException if adding the genre exceeds the maximum allowed limit.
     */
    ArtistProfileDetailDto addGenreToArtistProfile(UUID userId, UUID genreId);

    /**
     * Removes a specified genre from an existing artist profile.
     *
     * @param userId  The unique identifier of the user whose artist profile will be modified.
     * @param genreId The unique identifier of the genre to remove.
     * @return The updated {@link ArtistProfileDetailDto} reflecting the removed genre.
     * @throws com.spshpau.userservice.services.exceptions.ProfileNotFoundException if the artist profile does not exist.
     * @throws com.spshpau.userservice.services.exceptions.GenreNotFoundException if the genre with the given ID does not exist or was not associated.
     */
    ArtistProfileDetailDto removeGenreFromArtistProfile(UUID userId, UUID genreId);

    /**
     * Adds a specified skill to an existing artist profile.
     *
     * @param userId  The unique identifier of the user whose artist profile will be modified.
     * @param skillId The unique identifier of the skill to add.
     * @return The updated {@link ArtistProfileDetailDto} reflecting the added skill.
     * @throws com.spshpau.userservice.services.exceptions.ProfileNotFoundException if the artist profile does not exist.
     * @throws com.spshpau.userservice.services.exceptions.SkillNotFoundException if the skill with the given ID does not exist.
     * @throws com.spshpau.userservice.services.exceptions.SkillLimitExceededException if adding the skill exceeds the maximum allowed limit.
     */
    ArtistProfileDetailDto addSkillToArtistProfile(UUID userId, UUID skillId);

    /**
     * Removes a specified skill from an existing artist profile.
     *
     * @param userId  The unique identifier of the user whose artist profile will be modified.
     * @param skillId The unique identifier of the skill to remove.
     * @return The updated {@link ArtistProfileDetailDto} reflecting the removed skill.
     * @throws com.spshpau.userservice.services.exceptions.ProfileNotFoundException if the artist profile does not exist.
     * @throws com.spshpau.userservice.services.exceptions.SkillNotFoundException if the skill with the given ID does not exist or was not associated.
     */
    ArtistProfileDetailDto removeSkillFromArtistProfile(UUID userId, UUID skillId);

    /**
     * Retrieves all genres associated with a specific artist profile.
     *
     * @param userId The unique identifier of the user whose artist profile genres are to be retrieved.
     * @return A {@link Set} of {@link GenreSummaryDto} associated with the profile. Returns an empty set if no genres are associated or profile not found.
     * @throws com.spshpau.userservice.services.exceptions.ProfileNotFoundException if the artist profile for the user ID does not exist.
     */
    Set<GenreSummaryDto> getArtistProfileGenres(UUID userId);

    /**
     * Retrieves all skills associated with a specific artist profile.
     *
     * @param userId The unique identifier of the user whose artist profile skills are to be retrieved.
     * @return A {@link Set} of {@link SkillSummaryDto} associated with the profile. Returns an empty set if no skills are associated or profile not found.
     * @throws com.spshpau.userservice.services.exceptions.ProfileNotFoundException if the artist profile for the user ID does not exist.
     */
    Set<SkillSummaryDto> getArtistProfileSkills(UUID userId);

    /**
     * Retrieves the detailed artist profile for a given username.
     *
     * @param username The username of the user whose artist profile is to be retrieved.
     * @return An {@link Optional} containing the {@link ArtistProfileDetailDto} if a user with the username
     * and an associated artist profile is found, otherwise an empty Optional.
     */
    Optional<ArtistProfileDetailDto> getArtistProfileByUsername(String username);

    /**
     * Retrieves all genres associated with an artist profile identified by username.
     *
     * @param username The username of the user whose artist profile genres are to be retrieved.
     * @return A {@link Set} of {@link GenreSummaryDto} associated with the artist profile.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if no user exists with the given username.
     * @throws com.spshpau.userservice.services.exceptions.ProfileNotFoundException if the user exists but does not have an artist profile.
     */
    Set<GenreSummaryDto> getArtistProfileGenresByUsername(String username);

    /**
     * Retrieves all skills associated with an artist profile identified by username.
     *
     * @param username The username of the user whose artist profile skills are to be retrieved.
     * @return A {@link Set} of {@link SkillSummaryDto} associated with the artist profile.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if no user exists with the given username.
     * @throws com.spshpau.userservice.services.exceptions.ProfileNotFoundException if the user exists but does not have an artist profile.
     */
    Set<SkillSummaryDto> getArtistProfileSkillsByUsername(String username);
}
