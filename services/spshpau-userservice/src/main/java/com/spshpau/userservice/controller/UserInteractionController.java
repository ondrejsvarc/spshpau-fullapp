package com.spshpau.userservice.controller;

import com.spshpau.userservice.dto.userdto.UserSummaryDto;
import com.spshpau.userservice.model.User;
import com.spshpau.userservice.services.enums.InteractionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;
import java.util.UUID;

public interface UserInteractionController {

    // --- Connections ---

    /**
     * Sends a connection request from the currently authenticated user to another user (addressee).
     *
     * @param addresseeId The UUID of the user to whom the connection request is being sent.
     * @param jwt         The JWT token of the authenticated user (requester).
     * @return ResponseEntity with 202 Accepted if the request is successfully initiated,
     * or an error status (e.g., if users are already connected, blocked, or not found).
     * Example Success Response (202 Accepted):
     * (Empty Body)
     */
    ResponseEntity<?> sendRequest(@PathVariable UUID addresseeId, Jwt jwt);

    /**
     * Accepts a pending connection request from another user (requester).
     * The authenticated user is the one accepting the request.
     *
     * @param requesterId The UUID of the user who sent the connection request.
     * @param jwt         The JWT token of the authenticated user (acceptor).
     * @return ResponseEntity with 200 OK if the request is successfully accepted,
     * or an error status (e.g., if the request doesn't exist, or users not found).
     * Example Success Response (200 OK):
     * (Empty Body, or could return the updated connection details if designed so)
     */
    ResponseEntity<?> acceptRequest(@PathVariable UUID requesterId, Jwt jwt);

    /**
     * Rejects a pending connection request from another user (requester).
     * The authenticated user is the one rejecting the request.
     *
     * @param requesterId The UUID of the user who sent the connection request.
     * @param jwt         The JWT token of the authenticated user (rejector).
     * @return ResponseEntity with 204 No Content if the request is successfully rejected,
     * or an error status (e.g., if the request doesn't exist).
     * Example Success Response (204 No Content):
     * (Empty Body)
     */
    ResponseEntity<Void> rejectRequest(@PathVariable UUID requesterId, Jwt jwt);

    /**
     * Removes an existing connection between the authenticated user and another user.
     *
     * @param otherUserId The UUID of the other user in the connection.
     * @param jwt         The JWT token of the authenticated user.
     * @return ResponseEntity with 204 No Content if the connection is successfully removed,
     * or an error status (e.g., if no connection exists).
     * Example Success Response (204 No Content):
     * (Empty Body)
     */
    ResponseEntity<Void> removeConnection(@PathVariable UUID otherUserId, Jwt jwt);

    /**
     * Retrieves a paginated list of users connected to the currently authenticated user.
     *
     * @param pageable Pagination information.
     * @param jwt      The JWT token of the authenticated user.
     * @return ResponseEntity containing a Page of {@link UserSummaryDto} representing the connections.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "content": [
     * {"id": "conn1-uuid", "username": "connectionOne", "firstName": "Conn", "lastName": "One", "location": "Toronto"},
     * {"id": "conn2-uuid", "username": "connectionTwo", "firstName": "Conn", "lastName": "Two", "location": "Vancouver"}
     * ],
     * "pageable": {...},
     * "totalPages": 1,
     * "totalElements": 2,
     * ...
     * }
     * }</pre>
     */
    ResponseEntity<Page<UserSummaryDto>> getMyConnections(Pageable pageable, Jwt jwt);

    /**
     * Retrieves a list of all users connected to the currently authenticated user (not paginated).
     *
     * @param jwt The JWT token of the authenticated user.
     * @return ResponseEntity containing a List of {@link UserSummaryDto} representing all connections.
     * Example Success Response (200 OK):
     * <pre>{@code
     * [
     * {"id": "conn1-uuid", "username": "connectionOne", "firstName": "Conn", "lastName": "One", "location": "Toronto"},
     * {"id": "conn2-uuid", "username": "connectionTwo", "firstName": "Conn", "lastName": "Two", "location": "Vancouver"}
     * ]
     * }</pre>
     */
    ResponseEntity<List<UserSummaryDto>> getAllMyConnections(Jwt jwt);

    /**
     * Retrieves a paginated list of pending incoming connection requests for the authenticated user.
     *
     * @param pageable Pagination information.
     * @param jwt      The JWT token of the authenticated user.
     * @return ResponseEntity containing a Page of {@link UserSummaryDto} representing users who sent requests.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "content": [
     * {"id": "req1-uuid", "username": "requesterOne", "firstName": "Req", "lastName": "One", "location": "London"}
     * ],
     * "pageable": {...},
     * "totalPages": 1,
     * "totalElements": 1,
     * ...
     * }
     * }</pre>
     */
    ResponseEntity<Page<UserSummaryDto>> getMyPendingIncoming(Pageable pageable, Jwt jwt);

    /**
     * Retrieves a paginated list of pending outgoing connection requests made by the authenticated user.
     *
     * @param pageable Pagination information.
     * @param jwt      The JWT token of the authenticated user.
     * @return ResponseEntity containing a Page of {@link UserSummaryDto} representing users to whom requests were sent.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "content": [
     * {"id": "addr1-uuid", "username": "addresseeOne", "firstName": "Addr", "lastName": "One", "location": "Paris"}
     * ],
     * "pageable": {...},
     * "totalPages": 1,
     * "totalElements": 1,
     * ...
     * }
     * }</pre>
     */
    ResponseEntity<Page<UserSummaryDto>> getMyPendingOutgoing(Pageable pageable, Jwt jwt);

    // --- Blocking ---

    /**
     * Blocks another user. The authenticated user is the blocker.
     * Any existing connection or pending requests between them will be removed.
     *
     * @param blockedId The UUID of the user to be blocked.
     * @param jwt       The JWT token of the authenticated user (blocker).
     * @return ResponseEntity with 200 OK if the user is successfully blocked,
     * or an error status (e.g., user not found).
     * Example Success Response (200 OK):
     * (Empty Body)
     */
    ResponseEntity<Void> blockUser(@PathVariable UUID blockedId, Jwt jwt);

    /**
     * Unblocks a previously blocked user. The authenticated user is the one performing the unblock.
     *
     * @param blockedId The UUID of the user to be unblocked.
     * @param jwt       The JWT token of the authenticated user.
     * @return ResponseEntity with 204 No Content if the user is successfully unblocked,
     * or an error status (e.g., user not found or was not blocked).
     * Example Success Response (204 No Content):
     * (Empty Body)
     */
    ResponseEntity<Void> unblockUser(@PathVariable UUID blockedId, Jwt jwt);

    /**
     * Retrieves a paginated list of users blocked by the currently authenticated user.
     * (Note: The return type in the interface is Page<User>, so the example reflects full User objects,
     * which might include sensitive information not usually exposed. Often this might return UserSummaryDto).
     *
     * @param pageable Pagination information.
     * @param jwt      The JWT token of the authenticated user.
     * @return ResponseEntity containing a Page of {@link User} objects representing blocked users.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "content": [
     * {
     * "id": "blocked1-uuid", "username": "blockedUserOne", "email": "blocked1@example.com",
     * "firstName": "Blocked", "lastName": "UserOne", "location": "Somewhere", "active": true,
     * "blockedUsers": [], "producerProfile": null, "artistProfile": null
     * }
     * ],
     * "pageable": {...},
     * "totalPages": 1,
     * "totalElements": 1,
     * ...
     * }
     * }</pre>
     */
    ResponseEntity<Page<User>> getMyBlockedUsers(Pageable pageable, Jwt jwt);

    // --- Status ---

    /**
     * Retrieves the interaction status between the currently authenticated user and another specified user.
     * This can indicate if they are connected, if there's a pending request, if one has blocked the other, etc.
     *
     * @param otherUserId The UUID of the other user to check the interaction status with.
     * @param jwt         The JWT token of the authenticated user.
     * @return ResponseEntity containing the {@link InteractionStatus} enum,
     * or an error status if a user is not found.
     * Example Success Response (200 OK):
     * <pre>{@code
     * "CONNECTION_ACCEPTED"
     * }</pre>
     * Or:
     * <pre>{@code
     * "BLOCKED_BY_YOU"
     * }</pre>
     */
    ResponseEntity<InteractionStatus> getInteractionStatus(@PathVariable UUID otherUserId, Jwt jwt);

}
