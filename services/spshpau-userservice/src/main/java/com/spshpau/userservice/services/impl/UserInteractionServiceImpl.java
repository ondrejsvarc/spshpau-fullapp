package com.spshpau.userservice.services.impl;

import com.spshpau.userservice.dto.userdto.UserSummaryDto;
import com.spshpau.userservice.model.User;
import com.spshpau.userservice.model.UserConnection;
import com.spshpau.userservice.model.enums.ConnectionStatus;
import com.spshpau.userservice.repositories.UserConnectionRepository;
import com.spshpau.userservice.repositories.UserRepository;
import com.spshpau.userservice.services.UserInteractionService;
import com.spshpau.userservice.services.enums.InteractionStatus;
import com.spshpau.userservice.services.exceptions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserInteractionServiceImpl implements UserInteractionService {

    private final UserRepository userRepository;
    private final UserConnectionRepository userConnectionRepository;

    private User findUserOrThrow(UUID userId) {
        log.debug("Attempting to find user with ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });
    }

    private void checkUserActive(User user) {
        if (!user.isActive()) {
            log.warn("User {} (ID: {}) is deactivated.", user.getUsername(), user.getId());
            throw new UserNotActiveException("User " + user.getUsername() + " is deactivated.");
        }
    }

    // --- Connections ---

    @Override
    @Transactional
    public UserConnection sendConnectionRequest(UUID requesterId, UUID addresseeId) {
        log.info("Attempting to send connection request from requester ID: {} to addressee ID: {}", requesterId, addresseeId);
        if (requesterId.equals(addresseeId)) {
            log.warn("Connection request failed: Requester ID {} and addressee ID {} are the same.", requesterId, addresseeId);
            throw new ConnectionException("Cannot connect with oneself.");
        }
        User requester = findUserOrThrow(requesterId);
        User addressee = findUserOrThrow(addresseeId);
        checkUserActive(requester);
        checkUserActive(addressee);

        if (isBlocked(requesterId, addresseeId)) {
            log.warn("Connection request failed: A block exists between users {} and {}.", requesterId, addresseeId);
            throw new BlockedException("Cannot send connection request; a block exists between users.");
        }

        Optional<UserConnection> existingConnection = userConnectionRepository.findConnectionBetweenUsers(requesterId, addresseeId);
        if (existingConnection.isPresent()) {
            log.warn("Connection request failed: Connection already exists or is pending between users {} and {}. Status: {}", requesterId, addresseeId, existingConnection.get().getStatus());
            throw new ConnectionException("Connection already exists or is pending between users.");
        }

        UserConnection newConnection = new UserConnection(requester, addressee);
        UserConnection savedConnection = userConnectionRepository.save(newConnection);
        log.info("Successfully sent connection request. Connection ID: {}, Requester: {}, Addressee: {}", savedConnection.getId(), requesterId, addresseeId);
        return savedConnection;
    }

    @Override
    @Transactional
    public UserConnection acceptConnectionRequest(UUID acceptorId, UUID requesterId) {
        log.info("Attempting to accept connection request for acceptor ID: {} from requester ID: {}", acceptorId, requesterId);
        UserConnection connection = userConnectionRepository.findByRequesterIdAndAddresseeIdAndStatus(
                        requesterId, acceptorId, ConnectionStatus.PENDING)
                .orElseThrow(() -> {
                    log.warn("Accept connection failed: Pending connection request not found from user {} for user {}", requesterId, acceptorId);
                    return new ConnectionException("Pending connection request not found from user " + requesterId);
                });

        checkUserActive(connection.getAddressee());
        checkUserActive(connection.getRequester());

        connection.setStatus(ConnectionStatus.ACCEPTED);
        connection.setAcceptTimestamp(LocalDateTime.now());
        UserConnection savedConnection = userConnectionRepository.save(connection);
        log.info("Successfully accepted connection request. Connection ID: {}, Acceptor: {}, Requester: {}", savedConnection.getId(), acceptorId, requesterId);
        return savedConnection;
    }

    @Override
    @Transactional
    public void rejectConnectionRequest(UUID rejectorId, UUID requesterId) {
        log.info("Attempting to reject connection request for rejector ID: {} from requester ID: {}", rejectorId, requesterId);
        UserConnection connection = userConnectionRepository.findByRequesterIdAndAddresseeIdAndStatus(
                        requesterId, rejectorId, ConnectionStatus.PENDING)
                .orElseThrow(() -> {
                    log.warn("Reject connection failed: Pending connection request not found from user {} for user {}", requesterId, rejectorId);
                    return new ConnectionException("Pending connection request not found from user " + requesterId);
                });

        userConnectionRepository.delete(connection);
        log.info("Successfully rejected and deleted connection request. Connection ID: {}, Rejector: {}, Requester: {}", connection.getId(), rejectorId, requesterId);
    }

    @Override
    @Transactional
    public void removeConnection(UUID userId1, UUID userId2) {
        log.info("Attempting to remove connection between user ID: {} and user ID: {}", userId1, userId2);
        UserConnection connection = userConnectionRepository.findConnectionBetweenUsers(userId1, userId2)
                .filter(conn -> conn.getStatus() == ConnectionStatus.ACCEPTED)
                .orElseThrow(() -> {
                    log.warn("Remove connection failed: Accepted connection not found between users {} and {}", userId1, userId2);
                    return new ConnectionException("Accepted connection not found between users " + userId1 + " and " + userId2);
                });

        userConnectionRepository.delete(connection);
        log.info("Successfully removed connection. Connection ID: {}, User1: {}, User2: {}", connection.getId(), userId1, userId2);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryDto> getConnectionsDto(UUID userId, Pageable pageable) {
        log.debug("Fetching connections DTO for user ID: {} with pageable: {}", userId, pageable);
        Page<UserConnection> connectionsPage = userConnectionRepository.findAcceptedConnectionsForUser(userId, ConnectionStatus.ACCEPTED, pageable);
        List<UserSummaryDto> dtoList = connectionsPage.getContent().stream()
                .map(conn -> conn.getRequester().getId().equals(userId) ? conn.getAddressee() : conn.getRequester())
                .map(user -> new UserSummaryDto(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getLocation()))
                .collect(Collectors.toList());
        log.debug("Found {} connections for user ID: {} on page {}/{}", dtoList.size(), userId, pageable.getPageNumber(), connectionsPage.getTotalPages());
        return new PageImpl<>(dtoList, pageable, connectionsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryDto> getAllConnectionsDto(UUID userId) {
        log.debug("Fetching all connections DTO for user ID: {}", userId);
        List<UserConnection> connections = userConnectionRepository.findAllAcceptedConnectionsForUser(userId, ConnectionStatus.ACCEPTED);
        List<UserSummaryDto> dtoList = connections.stream()
                .map(conn -> conn.getRequester().getId().equals(userId) ? conn.getAddressee() : conn.getRequester())
                .map(user -> new UserSummaryDto(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getLocation()))
                .toList();
        log.debug("Found {} total connections for user ID: {}", dtoList.size(), userId);
        return dtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryDto> getPendingIncomingRequestsDto(UUID userId, Pageable pageable) {
        log.debug("Fetching pending incoming requests DTO for user ID: {} with pageable: {}", userId, pageable);
        Page<UserConnection> requestsPage = userConnectionRepository.findByAddresseeIdAndStatus(userId, ConnectionStatus.PENDING, pageable);
        List<UserSummaryDto> dtoList = requestsPage.getContent().stream()
                .map(UserConnection::getRequester)
                .map(user -> new UserSummaryDto(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getLocation()))
                .collect(Collectors.toList());
        log.debug("Found {} pending incoming requests for user ID: {} on page {}/{}", dtoList.size(), userId, pageable.getPageNumber(), requestsPage.getTotalPages());
        return new PageImpl<>(dtoList, pageable, requestsPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserSummaryDto> getPendingOutgoingRequestsDto(UUID userId, Pageable pageable) {
        log.debug("Fetching pending outgoing requests DTO for user ID: {} with pageable: {}", userId, pageable);
        Page<UserConnection> requestsPage = userConnectionRepository.findByRequesterIdAndStatus(userId, ConnectionStatus.PENDING, pageable);
        List<UserSummaryDto> dtoList = requestsPage.getContent().stream()
                .map(UserConnection::getAddressee)
                .map(user -> new UserSummaryDto(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getLocation()))
                .collect(Collectors.toList());
        log.debug("Found {} pending outgoing requests for user ID: {} on page {}/{}", dtoList.size(), userId, pageable.getPageNumber(), requestsPage.getTotalPages());
        return new PageImpl<>(dtoList, pageable, requestsPage.getTotalElements());
    }


    // --- Blocking ---

    @Override
    @Transactional
    public void blockUser(UUID blockerId, UUID blockedId) {
        log.info("Attempting to block user ID: {} by blocker ID: {}", blockedId, blockerId);
        if (blockerId.equals(blockedId)) {
            log.warn("Block user failed: Blocker ID {} and blocked ID {} are the same.", blockerId, blockedId);
            throw new ConnectionException("Cannot block oneself.");
        }
        User blocker = findUserOrThrow(blockerId);
        User blocked = findUserOrThrow(blockedId);
        checkUserActive(blocker);

        userConnectionRepository.findConnectionBetweenUsers(blockerId, blockedId)
                .ifPresent(connection -> {
                    log.info("Removing existing connection (ID: {}, Status: {}) between blocker {} and blocked {} before blocking.",
                            connection.getId(), connection.getStatus(), blockerId, blockedId);
                    userConnectionRepository.delete(connection);
                });

        boolean added = blocker.getBlockedUsers().add(blocked);
        if(added) {
            userRepository.save(blocker);
            log.info("User ID: {} successfully blocked by user ID: {}", blockedId, blockerId);
        } else {
            log.info("User ID: {} was already blocked by user ID: {}. No changes made.", blockedId, blockerId);
        }
    }

    @Override
    @Transactional
    public void unblockUser(UUID blockerId, UUID blockedId) {
        log.info("Attempting to unblock user ID: {} by blocker ID: {}", blockedId, blockerId);
        User blocker = findUserOrThrow(blockerId);
        User blocked = findUserOrThrow(blockedId);

        boolean removed = blocker.getBlockedUsers().remove(blocked);
        if (removed) {
            userRepository.save(blocker);
            log.info("User ID: {} successfully unblocked by user ID: {}", blockedId, blockerId);
        } else {
            log.info("User ID: {} was not blocked by user ID: {}. No changes made.", blockedId, blockerId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getBlockedUsers(UUID userId, Pageable pageable) {
        log.debug("Fetching blocked users for user ID: {} with pageable: {}", userId, pageable);
        User user = findUserOrThrow(userId);
        List<User> blockedList = user.getBlockedUsers().stream().toList();
        log.debug("User ID: {} has {} blocked users in total.", userId, blockedList.size());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), blockedList.size());
        List<User> pageContent = (start <= end && start < blockedList.size()) ? blockedList.subList(start, end) : List.of();

        log.debug("Returning {} blocked users for user ID: {} on page {}/{}", pageContent.size(), userId, pageable.getPageNumber(), (blockedList.size() + pageable.getPageSize() -1)/pageable.getPageSize() );
        return new PageImpl<>(pageContent, pageable, blockedList.size());
    }

    // --- Status Checks ---

    @Override
    @Transactional(readOnly = true)
    public InteractionStatus checkInteractionStatus(UUID viewingUserId, UUID targetUserId) {
        log.debug("Checking interaction status between viewing user ID: {} and target user ID: {}", viewingUserId, targetUserId);
        if (viewingUserId.equals(targetUserId)) {
            log.debug("Viewing user and target user are the same ({}). InteractionStatus: NONE", viewingUserId);
            return InteractionStatus.NONE;
        }

        User viewingUser = findUserOrThrow(viewingUserId);
        User targetUser = findUserOrThrow(targetUserId);

        boolean blockedByYou = viewingUser.getBlockedUsers().contains(targetUser);
        boolean blockedByOther = targetUser.getBlockedUsers().contains(viewingUser);

        if (blockedByYou && blockedByOther) {
            log.debug("InteractionStatus for {} and {}: BLOCKED_MUTUAL", viewingUserId, targetUserId);
            return InteractionStatus.BLOCKED_MUTUAL;
        }
        if (blockedByYou) {
            log.debug("InteractionStatus for {} and {}: BLOCKED_BY_YOU", viewingUserId, targetUserId);
            return InteractionStatus.BLOCKED_BY_YOU;
        }
        if (blockedByOther) {
            log.debug("InteractionStatus for {} and {}: BLOCKED_BY_OTHER", viewingUserId, targetUserId);
            return InteractionStatus.BLOCKED_BY_OTHER;
        }

        Optional<UserConnection> connectionOpt = userConnectionRepository.findConnectionBetweenUsers(viewingUserId, targetUserId);

        InteractionStatus statusToReturn = InteractionStatus.NONE;
        if (connectionOpt.isPresent()) {
            UserConnection connection = connectionOpt.get();
            if (connection.getStatus() == ConnectionStatus.ACCEPTED) {
                statusToReturn = InteractionStatus.CONNECTION_ACCEPTED;
            } else if (connection.getRequester().getId().equals(viewingUserId)) {
                statusToReturn = InteractionStatus.PENDING_OUTGOING;
            } else {
                statusToReturn = InteractionStatus.PENDING_INCOMING;
            }
        }
        log.debug("InteractionStatus for {} and {}: {}", viewingUserId, targetUserId, statusToReturn);
        return statusToReturn;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlocked(UUID userId1, UUID userId2) {
        log.debug("Checking if a block exists between user ID: {} and user ID: {}", userId1, userId2);
        User user1 = findUserOrThrow(userId1);
        User user2 = findUserOrThrow(userId2);
        boolean isBlockedResult = user1.getBlockedUsers().contains(user2) || user2.getBlockedUsers().contains(user1);
        log.debug("Block status between {} and {}: {}", userId1, userId2, isBlockedResult);
        return isBlockedResult;
    }
}