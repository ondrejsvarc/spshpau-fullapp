package com.spshpau.userservice.services;

import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.dto.profiledto.ProducerProfileDetailDto;
import com.spshpau.userservice.dto.profiledto.ProfileUpdateDto;
import com.spshpau.userservice.model.Genre;
import com.spshpau.userservice.model.ProducerProfile;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProducerProfileService {

    /**
     * Retrieves the detailed producer profile for a given user ID.
     *
     * @param userId The unique identifier of the user whose producer profile is to be retrieved.
     * @return An {@link Optional} containing the {@link ProducerProfileDetailDto} if found,
     * otherwise an empty Optional.
     */
    Optional<ProducerProfileDetailDto> getProducerProfileByUserId(UUID userId);

    /**
     * Creates a new producer profile or fully updates an existing one for the specified user.
     * If a profile for the user ID already exists, it will be updated with the provided data.
     * If not, a new profile will be created.
     *
     * @param userId       The unique identifier of the user for whom the profile is being created or updated.
     * @param profileData  A {@link ProfileUpdateDto} containing the data to create or update the profile.
     * Must not be null. ExperienceLevel is required for new profile creation.
     * @return The created or updated {@link ProducerProfileDetailDto}.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if the user with the given ID does not exist.
     * @throws IllegalArgumentException if experienceLevel is null during new profile creation.
     */
    ProducerProfileDetailDto createOrUpdateProducerProfile(UUID userId, ProfileUpdateDto profileData);

    /**
     * Partially updates an existing producer profile for the specified user.
     * Only the non-null fields in the {@code profileUpdateDto} will be applied to the existing profile.
     *
     * @param userId            The unique identifier of the user whose producer profile is to be patched.
     * @param profileUpdateDto  A {@link ProfileUpdateDto} containing the fields to update. Must not be null.
     * @return The updated {@link ProducerProfileDetailDto}.
     * @throws com.spshpau.userservice.services.exceptions.ProfileNotFoundException if no producer profile exists for the given user ID.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if the user with the given ID does not exist (checked if profile needs user context).
     */
    ProducerProfileDetailDto patchProducerProfile(UUID userId, ProfileUpdateDto profileUpdateDto);

    /**
     * Adds a specified genre to an existing producer profile.
     *
     * @param userId  The unique identifier of the user whose producer profile will be modified.
     * @param genreId The unique identifier of the genre to add.
     * @return The updated {@link ProducerProfileDetailDto} reflecting the added genre.
     * @throws com.spshpau.userservice.services.exceptions.ProfileNotFoundException if the producer profile does not exist.
     * @throws com.spshpau.userservice.services.exceptions.GenreNotFoundException if the genre with the given ID does not exist.
     * @throws com.spshpau.userservice.services.exceptions.GenreLimitExceededException if adding the genre exceeds the maximum allowed limit.
     */
    ProducerProfileDetailDto addGenreToProducerProfile(UUID userId, UUID genreId);

    /**
     * Removes a specified genre from an existing producer profile.
     *
     * @param userId  The unique identifier of the user whose producer profile will be modified.
     * @param genreId The unique identifier of the genre to remove.
     * @return The updated {@link ProducerProfileDetailDto} reflecting the removed genre.
     * @throws com.spshpau.userservice.services.exceptions.ProfileNotFoundException if the producer profile does not exist.
     * @throws com.spshpau.userservice.services.exceptions.GenreNotFoundException if the genre with the given ID does not exist or was not associated.
     */
    ProducerProfileDetailDto removeGenreFromProducerProfile(UUID userId, UUID genreId);

    /**
     * Retrieves all genres associated with a specific producer profile.
     *
     * @param userId The unique identifier of the user whose producer profile genres are to be retrieved.
     * @return A {@link Set} of {@link GenreSummaryDto} associated with the profile. Returns an empty set if no genres are associated or profile not found.
     * @throws com.spshpau.userservice.services.exceptions.ProfileNotFoundException if the producer profile for the user ID does not exist.
     */
    Set<GenreSummaryDto> getProducerProfileGenres(UUID userId);

    /**
     * Retrieves the detailed producer profile for a given username.
     *
     * @param username The username of the user whose producer profile is to be retrieved.
     * @return An {@link Optional} containing the {@link ProducerProfileDetailDto} if a user with the username
     * and an associated producer profile is found, otherwise an empty Optional.
     */
    Optional<ProducerProfileDetailDto> getProducerProfileByUsername(String username);

    /**
     * Retrieves all genres associated with a producer profile identified by username.
     *
     * @param username The username of the user whose producer profile genres are to be retrieved.
     * @return A {@link Set} of {@link GenreSummaryDto} associated with the producer profile.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if no user exists with the given username.
     * @throws com.spshpau.userservice.services.exceptions.ProfileNotFoundException if the user exists but does not have a producer profile.
     */
    Set<GenreSummaryDto> getProducerProfileGenresByUsername(String username);
}
