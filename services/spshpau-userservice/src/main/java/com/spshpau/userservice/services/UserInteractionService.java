package com.spshpau.userservice.services;

import com.spshpau.userservice.dto.userdto.UserSummaryDto;
import com.spshpau.userservice.model.User;
import com.spshpau.userservice.model.UserConnection;
import com.spshpau.userservice.services.enums.InteractionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserInteractionService {
    // --- Connections ---

    /**
     * Initiates a connection request from a requester to an addressee.
     *
     * @param requesterId The unique identifier of the user sending the request.
     * @param addresseeId The unique identifier of the user to whom the request is sent.
     * @return The created {@link UserConnection} object representing the pending request.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if either the requester or addressee does not exist.
     * @throws com.spshpau.userservice.services.exceptions.UserNotActiveException if either user is not active.
     * @throws com.spshpau.userservice.services.exceptions.ConnectionException if users try to connect with themselves, or if a connection/request already exists.
     * @throws com.spshpau.userservice.services.exceptions.BlockedException if a block exists between the users.
     */
    UserConnection sendConnectionRequest(UUID requesterId, UUID addresseeId);

    /**
     * Accepts a pending connection request.
     * The acceptor is the user who was originally the addressee of the request.
     *
     * @param acceptorId  The unique identifier of the user accepting the request.
     * @param requesterId The unique identifier of the user who initially sent the request.
     * @return The updated {@link UserConnection} object with status ACCEPTED.
     * @throws com.spshpau.userservice.services.exceptions.ConnectionException if no pending connection request is found from the requester to the acceptor.
     * @throws com.spshpau.userservice.services.exceptions.UserNotActiveException if either user involved in the connection is not active.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if involved users cannot be found (implicitly checked by connection lookup).
     */
    UserConnection acceptConnectionRequest(UUID acceptorId, UUID requesterId);

    /**
     * Rejects a pending connection request.
     * The rejector is the user who was originally the addressee of the request.
     * This operation typically deletes the pending request.
     *
     * @param rejectorId  The unique identifier of the user rejecting the request.
     * @param requesterId The unique identifier of the user who initially sent the request.
     * @throws com.spshpau.userservice.services.exceptions.ConnectionException if no pending connection request is found from the requester to the rejector.
     */
    void rejectConnectionRequest(UUID rejectorId, UUID requesterId);

    /**
     * Removes an established (accepted) connection between two users.
     *
     * @param userId1 The unique identifier of one user in the connection.
     * @param userId2 The unique identifier of the other user in the connection.
     * @throws com.spshpau.userservice.services.exceptions.ConnectionException if no accepted connection is found between the two users.
     */
    void removeConnection(UUID userId1, UUID userId2);

    /**
     * Retrieves a paginated list of users who are connected (accepted connection) with the specified user.
     *
     * @param userId   The unique identifier of the user whose connections are to be retrieved.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link UserSummaryDto} representing the connected users.
     */
    Page<UserSummaryDto> getConnectionsDto(UUID userId, Pageable pageable);

    /**
     * Retrieves a list of all users (non-paginated) who are connected (accepted connection) with the specified user.
     *
     * @param userId The unique identifier of the user whose connections are to be retrieved.
     * @return A {@link List} of {@link UserSummaryDto} representing all connected users.
     */
    List<UserSummaryDto> getAllConnectionsDto(UUID userId);

    /**
     * Retrieves a paginated list of pending incoming connection requests for the specified user.
     * These are requests sent by other users to the specified user.
     *
     * @param userId   The unique identifier of the user (addressee) whose incoming requests are to be retrieved.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link UserSummaryDto} representing the users who sent the requests.
     */
    Page<UserSummaryDto> getPendingIncomingRequestsDto(UUID userId, Pageable pageable);

    /**
     * Retrieves a paginated list of pending outgoing connection requests made by the specified user.
     * These are requests sent by the specified user to other users.
     *
     * @param userId   The unique identifier of the user (requester) whose outgoing requests are to be retrieved.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link UserSummaryDto} representing the users to whom requests were sent.
     */
    Page<UserSummaryDto> getPendingOutgoingRequestsDto(UUID userId, Pageable pageable);

    // --- Blocking ---

    /**
     * Blocks a user (blockedId) by another user (blockerId).
     * This action also removes any existing connections or pending requests between them.
     *
     * @param blockerId The unique identifier of the user initiating the block.
     * @param blockedId The unique identifier of the user to be blocked.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if either the blocker or blocked user does not exist.
     * @throws com.spshpau.userservice.services.exceptions.UserNotActiveException if the blocker is not active.
     * @throws com.spshpau.userservice.services.exceptions.ConnectionException if a user attempts to block themselves.
     */
    void blockUser(UUID blockerId, UUID blockedId);

    /**
     * Unblocks a user (blockedId) by another user (blockerId).
     *
     * @param blockerId The unique identifier of the user who initiated the block.
     * @param blockedId The unique identifier of the user to be unblocked.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if either the blocker or blocked user does not exist.
     */
    void unblockUser(UUID blockerId, UUID blockedId);

    /**
     * Retrieves a paginated list of users who have been blocked by the specified user.
     *
     * @param userId   The unique identifier of the user (blocker) whose blocked list is to be retrieved.
     * @param pageable Pagination information.
     * @return A {@link Page} of {@link User} objects representing the blocked users.
     * Note: Returning full User objects might expose sensitive data; consider UserSummaryDto.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if the user (blocker) does not exist.
     */
    Page<User> getBlockedUsers(UUID userId, Pageable pageable);

    // --- Status Checks ---

    /**
     * Checks and returns the current interaction status between two users.
     * This includes states like connected, pending requests (incoming/outgoing), blocked (by you/by other/mutual), or none.
     *
     * @param viewingUserId The unique identifier of the user from whose perspective the status is checked.
     * @param targetUserId  The unique identifier of the other user.
     * @return The {@link InteractionStatus} enum representing their relationship.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if either user does not exist.
     */
    InteractionStatus checkInteractionStatus(UUID viewingUserId, UUID targetUserId);

    /**
     * Determines if there is any block relationship (in either direction) between two users.
     *
     * @param userId1 The unique identifier of the first user.
     * @param userId2 The unique identifier of the second user.
     * @return {@code true} if a block exists in either direction, {@code false} otherwise.
     * @throws com.spshpau.userservice.services.exceptions.UserNotFoundException if either user does not exist.
     */
    boolean isBlocked(UUID userId1, UUID userId2);
}