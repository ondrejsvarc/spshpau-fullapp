package com.spshpau.userservice.repositories;

import com.spshpau.userservice.model.UserConnection;
import com.spshpau.userservice.model.enums.ConnectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserConnectionRepository extends JpaRepository<UserConnection, UUID> {

    Optional<UserConnection> findByRequesterIdAndAddresseeIdAndStatus(UUID requesterId, UUID addresseeId, ConnectionStatus status);

    // Find if any connection (pending or accepted) exists between two users, regardless of direction
    @Query("SELECT uc FROM UserConnection uc WHERE " +
            "((uc.requester.id = :userId1 AND uc.addressee.id = :userId2) OR " +
            "(uc.requester.id = :userId2 AND uc.addressee.id = :userId1))")
    Optional<UserConnection> findConnectionBetweenUsers(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);

    // Find if a specific connection (pending or accepted) exists in one direction
    Optional<UserConnection> findByRequesterIdAndAddresseeId(UUID requesterId, UUID addresseeId);


    // Find incoming pending requests for a user
    Page<UserConnection> findByAddresseeIdAndStatus(UUID addresseeId, ConnectionStatus status, Pageable pageable);

    // Find outgoing pending requests for a user
    Page<UserConnection> findByRequesterIdAndStatus(UUID requesterId, ConnectionStatus status, Pageable pageable);

    // Find accepted connections for a user (where they are either requester or addressee)
    @Query("SELECT uc FROM UserConnection uc WHERE (uc.requester.id = :userId OR uc.addressee.id = :userId) AND uc.status = :status")
    Page<UserConnection> findAcceptedConnectionsForUser(@Param("userId") UUID userId, @Param("status") ConnectionStatus status, Pageable pageable);

    // Find all accepted connections for a user (where they are either requester or addressee) without pagination
    @Query("SELECT uc FROM UserConnection uc WHERE (uc.requester.id = :userId OR uc.addressee.id = :userId) AND uc.status = :status")
    List<UserConnection> findAllAcceptedConnectionsForUser(@Param("userId") UUID userId, @Param("status") ConnectionStatus status);

    // Delete connection between two users
    void deleteByRequesterIdAndAddresseeId(UUID requesterId, UUID addresseeId);
}
