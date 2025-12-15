package com.spshpau.userservice.services;

import com.spshpau.userservice.dto.userdto.UserDetailDto;
import com.spshpau.userservice.dto.userdto.UserSearchCriteria;
import com.spshpau.userservice.dto.userdto.UserSummaryDto;
import com.spshpau.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    /**
     * Synchronizes user information from Keycloak (or another identity provider) into the local database.
     * If the user exists locally (identified by keycloakId), their details (username, email, names) are updated.
     * If the user does not exist, a new user record is created. The user is marked as active upon sync.
     *
     * @param keycloakId The user's unique identifier from Keycloak (typically the token subject).
     * @param username   The username from Keycloak.
     * @param email      The email from Keycloak.
     * @param firstName  The first name from Keycloak.
     * @param lastName   The last name from Keycloak.
     * @return A {@link UserDetailDto} representing the synchronized (created or updated) user.
     */
    UserDetailDto syncUserFromKeycloak(UUID keycloakId, String username, String email, String firstName, String lastName);

    /**
     * Updates the location for a specified user.
     *
     * @param userId   The unique identifier of the user whose location is to be updated.
     * @param location The new location string for the user.
     * @return A {@link UserDetailDto} representing the updated user.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if the user with the given ID is not found.
     */
    UserDetailDto updateUserLocation(UUID userId, String location);

    /**
     * Retrieves detailed information for a user by their unique ID.
     * This includes full profile details if available (artist and/or producer).
     *
     * @param userId The unique identifier of the user to retrieve.
     * @return An {@link Optional} containing the {@link UserDetailDto} if the user is found,
     * otherwise an empty Optional.
     */
    Optional<UserDetailDto> getUserDetailById(UUID userId);

    /**
     * Retrieves detailed information for a user by their username.
     * This includes full profile details if available (artist and/or producer).
     *
     * @param username The username of the user to retrieve.
     * @return An {@link Optional} containing the {@link UserDetailDto} if the user is found,
     * otherwise an empty Optional.
     */
    Optional<UserDetailDto> getUserDetailByUsername(String username);

    /**
     * Retrieves the raw {@link User} entity by its unique ID.
     * Use with caution, as this returns the JPA entity which might have implications
     * for lazy loading and direct exposure outside the service layer.
     *
     * @param userId The unique identifier of the user entity to retrieve.
     * @return An {@link Optional} containing the {@link User} entity if found,
     * otherwise an empty Optional.
     */
    Optional<User> getUserEntityById(UUID userId);

    /**
     * Deactivates a user account, effectively marking them as inactive (e.g., banned).
     * An inactive user might be restricted from certain actions or visibility.
     *
     * @param userId The unique identifier of the user to deactivate.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if the user with the given ID is not found.
     */
    void deactivateUser(UUID userId);

    /**
     * Reactivates a previously deactivated user account.
     *
     * @param userId The unique identifier of the user to reactivate.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if the user with the given ID is not found.
     */
    void reactivateUser(UUID userId);

    /**
     * Finds active users based on a set of search criteria, excluding the current user.
     *
     * @param currentUserId The unique identifier of the user initiating the search (to be excluded from results).
     * @param criteria      A {@link UserSearchCriteria} object containing various filter parameters
     * (e.g., searchTerm, genreIds, skillIds, profile existence, experience levels, availability).
     * @param pageable      Pagination information (page number, size, sort order).
     * @return A {@link Page} of {@link UserSummaryDto} objects matching the criteria.
     */
    Page<UserSummaryDto> findActiveUsers(UUID currentUserId, UserSearchCriteria criteria, Pageable pageable);

    /**
     * Finds matching users for the {@code currentUserId} based on a predefined matching algorithm.
     * This algorithm typically considers factors like:
     * <ul>
     * <li>Complementary profile types (e.g., an artist searching for producers, a producer searching for artists).</li>
     * <li>Overlap in musical genres.</li>
     * <li>Availability of the potential match.</li>
     * <li>Experience level compatibility.</li>
     * </ul>
     * The search excludes the current user, inactive users, and users with whom a block relationship exists.
     * Results are typically ranked by a match score. This operation may be cached.
     *
     * @param currentUserId The unique identifier of the user for whom matches are being sought.
     * This user must be active and have at least one profile (artist or producer) to find matches.
     * @param pageable      Pagination information (page number, size, sort order).
     * @return A {@link Page} of {@link UserSummaryDto} objects representing matched users,
     * sorted by match score (descending) and then by username.
     * Returns an empty page if the current user has no profile or no matches are found.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if an active user with the {@code currentUserId} is not found.
     */
    Page<UserSummaryDto> findMatches(UUID currentUserId, Pageable pageable);
}
