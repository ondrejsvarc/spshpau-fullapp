package com.spshpau.userservice.controller.impl;

import com.spshpau.userservice.controller.UserInteractionController;
import com.spshpau.userservice.dto.userdto.UserSummaryDto;
import com.spshpau.userservice.model.User;
import com.spshpau.userservice.services.enums.InteractionStatus;
import com.spshpau.userservice.services.UserInteractionService;
import com.spshpau.userservice.services.exceptions.BlockedException;
import com.spshpau.userservice.services.exceptions.ConnectionException;
import com.spshpau.userservice.services.exceptions.UserNotActiveException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/interactions/me")
@RequiredArgsConstructor
public class UserInteractionControllerImpl implements UserInteractionController {
    private final UserInteractionService userInteractionService;

    // Helper Method
    private UUID getUserIdFromJwt(Jwt jwt) {
        if (jwt == null) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication token is missing"); }
        String subject = jwt.getSubject();
        if (subject == null) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token missing subject claim"); }
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user identifier in token");
        }
    }

    // --- Connections ---

    @Override
    @PostMapping("/connections/request/{addresseeId}")
    public ResponseEntity<?> sendRequest(@PathVariable UUID addresseeId, @AuthenticationPrincipal Jwt jwt) {
        UUID requesterId = getUserIdFromJwt(jwt);
        try {
            userInteractionService.sendConnectionRequest(requesterId, addresseeId);
            return ResponseEntity.accepted().build();
        } catch (EntityNotFoundException | UserNotActiveException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (ConnectionException | BlockedException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error sending request", ex);
        }
    }

    @Override
    @PostMapping("/connections/accept/{requesterId}")
    public ResponseEntity<?> acceptRequest(@PathVariable UUID requesterId, @AuthenticationPrincipal Jwt jwt) {
        UUID acceptorId = getUserIdFromJwt(jwt);
        try {
            userInteractionService.acceptConnectionRequest(acceptorId, requesterId);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException | UserNotActiveException | ConnectionException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error accepting request", ex);
        }
    }

    @Override
    @DeleteMapping("/connections/reject/{requesterId}")
    public ResponseEntity<Void> rejectRequest(@PathVariable UUID requesterId, @AuthenticationPrincipal Jwt jwt) {
        UUID rejectorId = getUserIdFromJwt(jwt);
        try {
            userInteractionService.rejectConnectionRequest(rejectorId, requesterId);
            return ResponseEntity.noContent().build();
        } catch (ConnectionException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error rejecting request", ex);
        }
    }

    @Override
    @DeleteMapping("/connections/remove/{otherUserId}")
    public ResponseEntity<Void> removeConnection(@PathVariable UUID otherUserId, @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        try {
            userInteractionService.removeConnection(currentUserId, otherUserId);
            return ResponseEntity.noContent().build();
        } catch (ConnectionException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error removing connection", ex);
        }
    }

    @Override
    @GetMapping("/connections")
    public ResponseEntity<Page<UserSummaryDto>> getMyConnections(@PageableDefault(size=20) Pageable pageable, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = getUserIdFromJwt(jwt);
        return ResponseEntity.ok(userInteractionService.getConnectionsDto(userId, pageable));
    }

    @Override
    @GetMapping("/connections/all")
    public ResponseEntity<List<UserSummaryDto>> getAllMyConnections(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = getUserIdFromJwt(jwt);
        return ResponseEntity.ok(userInteractionService.getAllConnectionsDto(userId));
    }

    @Override
    @GetMapping("/connections/requests/incoming")
    public ResponseEntity<Page<UserSummaryDto>> getMyPendingIncoming(@PageableDefault(size=20) Pageable pageable, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = getUserIdFromJwt(jwt);
        return ResponseEntity.ok(userInteractionService.getPendingIncomingRequestsDto(userId, pageable));
    }

    @Override
    @GetMapping("/connections/requests/outgoing")
    public ResponseEntity<Page<UserSummaryDto>> getMyPendingOutgoing(@PageableDefault(size=20) Pageable pageable, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = getUserIdFromJwt(jwt);
        return ResponseEntity.ok(userInteractionService.getPendingOutgoingRequestsDto(userId, pageable));
    }


    // --- Blocking ---

    @Override
    @PostMapping("/blocks/block/{blockedId}")
    public ResponseEntity<Void> blockUser(@PathVariable UUID blockedId, @AuthenticationPrincipal Jwt jwt) {
        UUID blockerId = getUserIdFromJwt(jwt);
        try {
            userInteractionService.blockUser(blockerId, blockedId);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException | UserNotActiveException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (ConnectionException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error blocking user", ex);
        }
    }

    @Override
    @DeleteMapping("/blocks/unblock/{blockedId}")
    public ResponseEntity<Void> unblockUser(@PathVariable UUID blockedId, @AuthenticationPrincipal Jwt jwt) {
        UUID blockerId = getUserIdFromJwt(jwt);
        try {
            userInteractionService.unblockUser(blockerId, blockedId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error unblocking user", ex);
        }
    }

    @Override
    @GetMapping("/blocks")
    public ResponseEntity<Page<User>> getMyBlockedUsers(@PageableDefault(size=20) Pageable pageable, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = getUserIdFromJwt(jwt);
        try {
            return ResponseEntity.ok(userInteractionService.getBlockedUsers(userId, pageable));
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    // --- Status ---

    @Override
    @GetMapping("/status/{otherUserId}")
    public ResponseEntity<InteractionStatus> getInteractionStatus(@PathVariable UUID otherUserId, @AuthenticationPrincipal Jwt jwt) {
        UUID currentUserId = getUserIdFromJwt(jwt);
        try {
            InteractionStatus status = userInteractionService.checkInteractionStatus(currentUserId, otherUserId);
            return ResponseEntity.ok(status);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }
}
