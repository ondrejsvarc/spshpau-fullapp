package com.spshpau.userservice.repositories;

import com.spshpau.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    /**
     * Finds a user by their username.
     * @param username The username to search for.
     * @return An Optional containing the User if found, otherwise empty.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     * @param email The email address to search for.
     * @return An Optional containing the User if found, otherwise empty.
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds the IDs of users blocked by the specified user.
     * @param userId The UUID of the user doing the blocking (blocker).
     * @return A Set of UUIDs of the users who are blocked.
     */
    @Query("SELECT bu.id FROM User u JOIN u.blockedUsers bu WHERE u.id = :userId")
    Set<UUID> findBlockedUserIdsByBlockerId(@Param("userId") UUID userId);

    /**
     * Finds the IDs of users who have blocked the specified user.
     * @param userId The UUID of the user who might be blocked.
     * @return A Set of UUIDs of the users who are blocking the given user.
     */
    @Query("SELECT u.id FROM User u JOIN u.blockedUsers bu WHERE bu.id = :userId")
    Set<UUID> findBlockerUserIdsByBlockedId(@Param("userId") UUID userId);
}
