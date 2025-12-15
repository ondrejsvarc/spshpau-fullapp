package com.spshpau.userservice.services.impl;

import com.spshpau.userservice.dto.userdto.UserSummaryDto;
import com.spshpau.userservice.model.User;
import com.spshpau.userservice.model.UserConnection;
import com.spshpau.userservice.model.enums.ConnectionStatus;
import com.spshpau.userservice.repositories.UserConnectionRepository;
import com.spshpau.userservice.repositories.UserRepository;
import com.spshpau.userservice.services.enums.InteractionStatus;
import com.spshpau.userservice.services.exceptions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserInteractionServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserConnectionRepository userConnectionRepository;

    @InjectMocks
    private UserInteractionServiceImpl userInteractionService;

    private User requester;
    private User addressee;
    private User blocker;
    private User blocked;
    private UUID requesterId;
    private UUID addresseeId;
    private UUID blockerId;
    private UUID blockedId;

    @BeforeEach
    void setUp() {
        requesterId = UUID.randomUUID();
        addresseeId = UUID.randomUUID();
        blockerId = UUID.randomUUID();
        blockedId = UUID.randomUUID();

        requester = new User();
        requester.setId(requesterId);
        requester.setUsername("requester");
        requester.setActive(true);
        requester.setBlockedUsers(new HashSet<>());


        addressee = new User();
        addressee.setId(addresseeId);
        addressee.setUsername("addressee");
        addressee.setActive(true);
        addressee.setBlockedUsers(new HashSet<>());

        blocker = new User();
        blocker.setId(blockerId);
        blocker.setUsername("blocker");
        blocker.setActive(true);
        blocker.setBlockedUsers(new HashSet<>());

        blocked = new User();
        blocked.setId(blockedId);
        blocked.setUsername("blockedUser");
        blocked.setActive(true);
        blocked.setBlockedUsers(new HashSet<>());
    }

    // --- Tests for sendConnectionRequest ---
    @Test
    void sendConnectionRequest_validRequest_shouldSaveConnection() {
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));
        when(userConnectionRepository.findConnectionBetweenUsers(requesterId, addresseeId)).thenReturn(Optional.empty());
        when(userConnectionRepository.save(any(UserConnection.class))).thenAnswer(invocation -> {
            UserConnection conn = invocation.getArgument(0);
            conn.setId(UUID.randomUUID());
            return conn;
        });

        UserConnection result = userInteractionService.sendConnectionRequest(requesterId, addresseeId);

        assertNotNull(result);
        assertEquals(requester, result.getRequester());
        assertEquals(addressee, result.getAddressee());
        assertEquals(ConnectionStatus.PENDING, result.getStatus());
        verify(userConnectionRepository).save(any(UserConnection.class));
    }

    @Test
    void sendConnectionRequest_selfConnection_shouldThrowConnectionException() {
        assertThrows(ConnectionException.class, () -> {
            userInteractionService.sendConnectionRequest(requesterId, requesterId);
        });
        verify(userRepository, never()).findById(any());
    }

    @Test
    void sendConnectionRequest_requesterNotFound_shouldThrowUserNotFoundException() {
        when(userRepository.findById(requesterId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userInteractionService.sendConnectionRequest(requesterId, addresseeId);
        });
    }

    @Test
    void sendConnectionRequest_addresseeNotFound_shouldThrowUserNotFoundException() {
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userInteractionService.sendConnectionRequest(requesterId, addresseeId);
        });
    }


    @Test
    void sendConnectionRequest_requesterNotActive_shouldThrowUserNotActiveException() {
        requester.setActive(false);
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));

        assertThrows(UserNotActiveException.class, () -> {
            userInteractionService.sendConnectionRequest(requesterId, addresseeId);
        });
    }

    @Test
    void sendConnectionRequest_addresseeNotActive_shouldThrowUserNotActiveException() {
        addressee.setActive(false);
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));

        assertThrows(UserNotActiveException.class, () -> {
            userInteractionService.sendConnectionRequest(requesterId, addresseeId);
        });
    }

    @Test
    void sendConnectionRequest_userBlocked_shouldThrowBlockedException() {
        requester.getBlockedUsers().add(addressee);
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));

        assertThrows(BlockedException.class, () -> {
            userInteractionService.sendConnectionRequest(requesterId, addresseeId);
        });
    }

    @Test
    void sendConnectionRequest_connectionAlreadyExists_shouldThrowConnectionException() {
        UserConnection existingConnection = new UserConnection(requester, addressee);
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));
        when(userConnectionRepository.findConnectionBetweenUsers(requesterId, addresseeId)).thenReturn(Optional.of(existingConnection));

        assertThrows(ConnectionException.class, () -> {
            userInteractionService.sendConnectionRequest(requesterId, addresseeId);
        });
    }

    // --- Tests for acceptConnectionRequest ---
    @Test
    void acceptConnectionRequest_valid_shouldAccept() {
        UserConnection pendingConnection = new UserConnection(requester, addressee);
        pendingConnection.setStatus(ConnectionStatus.PENDING);

        when(userConnectionRepository.findByRequesterIdAndAddresseeIdAndStatus(requesterId, addresseeId, ConnectionStatus.PENDING))
                .thenReturn(Optional.of(pendingConnection));

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));


        ArgumentCaptor<UserConnection> connectionCaptor = ArgumentCaptor.forClass(UserConnection.class);
        when(userConnectionRepository.save(connectionCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        UserConnection result = userInteractionService.acceptConnectionRequest(addresseeId, requesterId);

        assertNotNull(result);
        assertEquals(ConnectionStatus.ACCEPTED, result.getStatus());
        assertNotNull(result.getAcceptTimestamp());
        verify(userConnectionRepository).save(pendingConnection);

        UserConnection captured = connectionCaptor.getValue();
        assertEquals(ConnectionStatus.ACCEPTED, captured.getStatus());
    }

    @Test
    void acceptConnectionRequest_requestNotFound_shouldThrowConnectionException() {
        when(userConnectionRepository.findByRequesterIdAndAddresseeIdAndStatus(requesterId, addresseeId, ConnectionStatus.PENDING))
                .thenReturn(Optional.empty());

        assertThrows(ConnectionException.class, () -> {
            userInteractionService.acceptConnectionRequest(addresseeId, requesterId);
        });
    }

    @Test
    void acceptConnectionRequest_acceptorNotActive_shouldThrowUserNotActiveException() {
        UserConnection pendingConnection = new UserConnection(requester, addressee);
        pendingConnection.setStatus(ConnectionStatus.PENDING);
        addressee.setActive(false);

        when(userConnectionRepository.findByRequesterIdAndAddresseeIdAndStatus(requesterId, addresseeId, ConnectionStatus.PENDING))
                .thenReturn(Optional.of(pendingConnection));

        assertThrows(UserNotActiveException.class, () -> {
            userInteractionService.acceptConnectionRequest(addresseeId, requesterId);
        });
    }


    // --- Tests for rejectConnectionRequest ---
    @Test
    void rejectConnectionRequest_valid_shouldDelete() {
        UserConnection pendingConnection = new UserConnection(requester, addressee);
        pendingConnection.setStatus(ConnectionStatus.PENDING);
        when(userConnectionRepository.findByRequesterIdAndAddresseeIdAndStatus(requesterId, addresseeId, ConnectionStatus.PENDING))
                .thenReturn(Optional.of(pendingConnection));

        userInteractionService.rejectConnectionRequest(addresseeId, requesterId);

        verify(userConnectionRepository).delete(pendingConnection);
    }

    // --- Tests for removeConnection ---
    @Test
    void removeConnection_valid_shouldDelete() {
        UserConnection acceptedConnection = new UserConnection(requester, addressee);
        acceptedConnection.setStatus(ConnectionStatus.ACCEPTED);
        when(userConnectionRepository.findConnectionBetweenUsers(requesterId, addresseeId))
                .thenReturn(Optional.of(acceptedConnection));

        userInteractionService.removeConnection(requesterId, addresseeId);
        verify(userConnectionRepository).delete(acceptedConnection);
    }

    // --- Tests for getConnectionsDto ---
    @Test
    void getConnectionsDto_shouldReturnPageOfSummaries() {
        Pageable pageable = PageRequest.of(0, 10);
        User user3 = new User(); user3.setId(UUID.randomUUID()); user3.setUsername("user3");
        UserConnection conn1 = new UserConnection(requester, addressee); conn1.setStatus(ConnectionStatus.ACCEPTED);
        UserConnection conn2 = new UserConnection(user3, requester); conn2.setStatus(ConnectionStatus.ACCEPTED);
        List<UserConnection> connections = List.of(conn1, conn2);
        Page<UserConnection> connectionPage = new PageImpl<>(connections, pageable, connections.size());

        when(userConnectionRepository.findAcceptedConnectionsForUser(requesterId, ConnectionStatus.ACCEPTED, pageable))
                .thenReturn(connectionPage);

        Page<UserSummaryDto> result = userInteractionService.getConnectionsDto(requesterId, pageable);

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(dto -> dto.getUsername().equals(addressee.getUsername())));
        assertTrue(result.getContent().stream().anyMatch(dto -> dto.getUsername().equals(user3.getUsername())));
    }

    // --- Tests for blockUser ---
    @Test
    void blockUser_valid_shouldBlockAndRemoveConnection() {
        UserConnection existingConnection = new UserConnection(blocker, blocked);
        existingConnection.setStatus(ConnectionStatus.ACCEPTED);

        when(userRepository.findById(blockerId)).thenReturn(Optional.of(blocker));
        when(userRepository.findById(blockedId)).thenReturn(Optional.of(blocked));
        when(userConnectionRepository.findConnectionBetweenUsers(blockerId, blockedId)).thenReturn(Optional.of(existingConnection));

        userInteractionService.blockUser(blockerId, blockedId);

        assertTrue(blocker.getBlockedUsers().contains(blocked));
        verify(userConnectionRepository).delete(existingConnection);
        verify(userRepository).save(blocker);
    }

    @Test
    void blockUser_valid_noExistingConnection_shouldBlock() {
        when(userRepository.findById(blockerId)).thenReturn(Optional.of(blocker));
        when(userRepository.findById(blockedId)).thenReturn(Optional.of(blocked));
        when(userConnectionRepository.findConnectionBetweenUsers(blockerId, blockedId)).thenReturn(Optional.empty());

        userInteractionService.blockUser(blockerId, blockedId);

        assertTrue(blocker.getBlockedUsers().contains(blocked));
        verify(userConnectionRepository, never()).delete(any());
        verify(userRepository).save(blocker);
    }


    @Test
    void blockUser_selfBlock_shouldThrowConnectionException() {
        assertThrows(ConnectionException.class, () -> {
            userInteractionService.blockUser(blockerId, blockerId);
        });
    }

    @Test
    void blockUser_blockerNotFound_shouldThrowUserNotFoundException() {
        when(userRepository.findById(blockerId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userInteractionService.blockUser(blockerId, blockedId);
        });
    }

    @Test
    void blockUser_blockedUserNotFound_shouldThrowUserNotFoundException() {
        when(userRepository.findById(blockerId)).thenReturn(Optional.of(blocker));
        when(userRepository.findById(blockedId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userInteractionService.blockUser(blockerId, blockedId);
        });
    }


    @Test
    void blockUser_blockerNotActive_shouldThrowUserNotActiveException() {
        blocker.setActive(false);
        when(userRepository.findById(blockerId)).thenReturn(Optional.of(blocker));
        when(userRepository.findById(blockedId)).thenReturn(Optional.of(blocked));

        assertThrows(UserNotActiveException.class, () -> {
            userInteractionService.blockUser(blockerId, blockedId);
        });
    }

    @Test
    void blockUser_alreadyBlocked_shouldNotSaveAgain() {
        blocker.getBlockedUsers().add(blocked);

        when(userRepository.findById(blockerId)).thenReturn(Optional.of(blocker));
        when(userRepository.findById(blockedId)).thenReturn(Optional.of(blocked));
        when(userConnectionRepository.findConnectionBetweenUsers(blockerId, blockedId)).thenReturn(Optional.empty());

        userInteractionService.blockUser(blockerId, blockedId);

        verify(userRepository, never()).save(blocker);
    }


    // --- Tests for unblockUser ---
    @Test
    void unblockUser_valid_shouldUnblock() {
        blocker.getBlockedUsers().add(blocked);
        when(userRepository.findById(blockerId)).thenReturn(Optional.of(blocker));
        when(userRepository.findById(blockedId)).thenReturn(Optional.of(blocked));

        userInteractionService.unblockUser(blockerId, blockedId);

        assertFalse(blocker.getBlockedUsers().contains(blocked));
        verify(userRepository).save(blocker);
    }

    @Test
    void unblockUser_notActuallyBlocked_shouldNotSave() {
        assertFalse(blocker.getBlockedUsers().contains(blocked));

        when(userRepository.findById(blockerId)).thenReturn(Optional.of(blocker));
        when(userRepository.findById(blockedId)).thenReturn(Optional.of(blocked));

        userInteractionService.unblockUser(blockerId, blockedId);

        assertFalse(blocker.getBlockedUsers().contains(blocked));
        verify(userRepository, never()).save(blocker);
    }


    // --- Tests for getBlockedUsers ---
    @Test
    void getBlockedUsers_shouldReturnPageOfBlockedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        User blockedUser1 = new User(); blockedUser1.setId(UUID.randomUUID());
        User blockedUser2 = new User(); blockedUser2.setId(UUID.randomUUID());
        blocker.setBlockedUsers(new HashSet<>(Set.of(blockedUser1, blockedUser2)));

        when(userRepository.findById(blockerId)).thenReturn(Optional.of(blocker));

        Page<User> result = userInteractionService.getBlockedUsers(blockerId, pageable);

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().contains(blockedUser1));
    }

    // --- Tests for checkInteractionStatus ---
    @Test
    void checkInteractionStatus_sameUser_shouldReturnNone() {
        assertEquals(InteractionStatus.NONE, userInteractionService.checkInteractionStatus(requesterId, requesterId));
        verify(userRepository, never()).findById(any());
    }

    @Test
    void checkInteractionStatus_blockedByYou_shouldReturnBlockedByYou() {
        requester.getBlockedUsers().add(addressee);
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));

        assertEquals(InteractionStatus.BLOCKED_BY_YOU, userInteractionService.checkInteractionStatus(requesterId, addresseeId));
    }

    @Test
    void checkInteractionStatus_blockedByOther_shouldReturnBlockedByOther() {
        addressee.getBlockedUsers().add(requester);
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));

        assertEquals(InteractionStatus.BLOCKED_BY_OTHER, userInteractionService.checkInteractionStatus(requesterId, addresseeId));
    }

    @Test
    void checkInteractionStatus_blockedMutual_shouldReturnBlockedMutual() {
        requester.getBlockedUsers().add(addressee);
        addressee.getBlockedUsers().add(requester);
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));

        assertEquals(InteractionStatus.BLOCKED_MUTUAL, userInteractionService.checkInteractionStatus(requesterId, addresseeId));
    }


    @Test
    void checkInteractionStatus_connectionAccepted_shouldReturnAccepted() {
        UserConnection acceptedConn = new UserConnection(requester, addressee);
        acceptedConn.setStatus(ConnectionStatus.ACCEPTED);
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));
        when(userConnectionRepository.findConnectionBetweenUsers(requesterId, addresseeId)).thenReturn(Optional.of(acceptedConn));

        assertEquals(InteractionStatus.CONNECTION_ACCEPTED, userInteractionService.checkInteractionStatus(requesterId, addresseeId));
    }

    @Test
    void checkInteractionStatus_pendingOutgoing_shouldReturnPendingOutgoing() {
        UserConnection pendingConn = new UserConnection(requester, addressee);
        pendingConn.setStatus(ConnectionStatus.PENDING);
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));
        when(userConnectionRepository.findConnectionBetweenUsers(requesterId, addresseeId)).thenReturn(Optional.of(pendingConn));

        // Viewing from requester's perspective
        assertEquals(InteractionStatus.PENDING_OUTGOING, userInteractionService.checkInteractionStatus(requesterId, addresseeId));
    }

    @Test
    void checkInteractionStatus_pendingIncoming_shouldReturnPendingIncoming() {
        UserConnection pendingConn = new UserConnection(addressee, requester);
        pendingConn.setStatus(ConnectionStatus.PENDING);
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));
        when(userConnectionRepository.findConnectionBetweenUsers(requesterId, addresseeId)).thenReturn(Optional.of(pendingConn));

        assertEquals(InteractionStatus.PENDING_INCOMING, userInteractionService.checkInteractionStatus(requesterId, addresseeId));
    }


    @Test
    void checkInteractionStatus_noInteraction_shouldReturnNone() {
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));
        when(userConnectionRepository.findConnectionBetweenUsers(requesterId, addresseeId)).thenReturn(Optional.empty());

        assertEquals(InteractionStatus.NONE, userInteractionService.checkInteractionStatus(requesterId, addresseeId));
    }

    // --- Tests for isBlocked ---
    @Test
    void isBlocked_user1BlocksUser2_shouldReturnTrue() {
        requester.getBlockedUsers().add(addressee);
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));
        assertTrue(userInteractionService.isBlocked(requesterId, addresseeId));
    }

    @Test
    void isBlocked_user2BlocksUser1_shouldReturnTrue() {
        addressee.getBlockedUsers().add(requester);
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));
        assertTrue(userInteractionService.isBlocked(requesterId, addresseeId));
    }


    @Test
    void isBlocked_noBlock_shouldReturnFalse() {
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(userRepository.findById(addresseeId)).thenReturn(Optional.of(addressee));
        assertFalse(userInteractionService.isBlocked(requesterId, addresseeId));
    }
}