package com.spshpau.projectservice.services.impl;

import com.spshpau.projectservice.dto.UserSummaryDto;
import com.spshpau.projectservice.model.SimpleUser;
import com.spshpau.projectservice.repositories.SimpleUserRepository;
import com.spshpau.projectservice.services.exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimpleUserServiceImplTest {

    @Mock
    private SimpleUserRepository simpleUserRepository;

    @InjectMocks
    private SimpleUserServiceImpl simpleUserService;

    private UUID testUserId;
    private SimpleUser testUser;
    private UserSummaryDto testUserSummaryDto;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new SimpleUser(testUserId, "testuser", "Test", "User", "TestLocation", null, null, null);
        testUserSummaryDto = UserSummaryDto.builder()
                .id(testUserId)
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .location("TestLocation")
                .build();
    }

    // Tests for findUserById(UUID userId)
    @Test
    void findUserById_whenUserExists_shouldReturnUser() {
        when(simpleUserRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        SimpleUser foundUser = simpleUserService.findUserById(testUserId);

        assertNotNull(foundUser);
        assertEquals(testUserId, foundUser.getId());
        assertEquals("testuser", foundUser.getUsername());
        verify(simpleUserRepository, times(1)).findById(testUserId);
    }

    @Test
    void findUserById_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
        when(simpleUserRepository.findById(testUserId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            simpleUserService.findUserById(testUserId);
        });

        assertEquals("User not found with ID: " + testUserId, exception.getMessage());
        verify(simpleUserRepository, times(1)).findById(testUserId);
    }

    // Tests for getOrCreateSimpleUser(UserSummaryDto userSummaryDto)
    @Test
    void getOrCreateSimpleUser_dto_whenUserExists_shouldReturnExistingUser() {
        when(simpleUserRepository.findById(testUserSummaryDto.getId())).thenReturn(Optional.of(testUser));

        SimpleUser resultUser = simpleUserService.getOrCreateSimpleUser(testUserSummaryDto);

        assertNotNull(resultUser);
        assertEquals(testUser.getId(), resultUser.getId());
        assertEquals(testUser.getUsername(), resultUser.getUsername());
        verify(simpleUserRepository, times(1)).findById(testUserSummaryDto.getId());
        verify(simpleUserRepository, never()).save(any(SimpleUser.class));
    }

    @Test
    void getOrCreateSimpleUser_dto_whenUserDoesNotExist_shouldCreateAndReturnNewUser() {
        when(simpleUserRepository.findById(testUserSummaryDto.getId())).thenReturn(Optional.empty());
        when(simpleUserRepository.save(any(SimpleUser.class))).thenAnswer(invocation -> {
            SimpleUser userToSave = invocation.getArgument(0);
            return userToSave;
        });

        SimpleUser resultUser = simpleUserService.getOrCreateSimpleUser(testUserSummaryDto);

        assertNotNull(resultUser);
        assertEquals(testUserSummaryDto.getId(), resultUser.getId());
        assertEquals(testUserSummaryDto.getUsername(), resultUser.getUsername());
        assertEquals(testUserSummaryDto.getFirstName(), resultUser.getFirstName());
        assertEquals(testUserSummaryDto.getLastName(), resultUser.getLastName());
        assertEquals(testUserSummaryDto.getLocation(), resultUser.getLocation());

        verify(simpleUserRepository, times(1)).findById(testUserSummaryDto.getId());
        verify(simpleUserRepository, times(1)).save(any(SimpleUser.class));
    }

    @Test
    void getOrCreateSimpleUser_dto_whenDtoIsNull_shouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            simpleUserService.getOrCreateSimpleUser((UserSummaryDto) null);
        });
        assertEquals("UserSummaryDto or its ID cannot be null", exception.getMessage());
        verify(simpleUserRepository, never()).findById(any());
        verify(simpleUserRepository, never()).save(any());
    }

    @Test
    void getOrCreateSimpleUser_dto_whenDtoIdIsNull_shouldThrowIllegalArgumentException() {
        testUserSummaryDto.setId(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            simpleUserService.getOrCreateSimpleUser(testUserSummaryDto);
        });
        assertEquals("UserSummaryDto or its ID cannot be null", exception.getMessage());
        verify(simpleUserRepository, never()).findById(any());
        verify(simpleUserRepository, never()).save(any());
    }

    // Tests for getOrCreateSimpleUser(UUID userId, String username, String firstName, String lastName, String location)
    @Test
    void getOrCreateSimpleUser_params_whenUserExists_shouldReturnExistingUser() {
        when(simpleUserRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        SimpleUser resultUser = simpleUserService.getOrCreateSimpleUser(testUserId, "newUsername", "NewFirst", "NewLast", "NewLocation");

        assertNotNull(resultUser);
        assertEquals(testUser.getId(), resultUser.getId());
        assertEquals(testUser.getUsername(), resultUser.getUsername());
        verify(simpleUserRepository, times(1)).findById(testUserId);
        verify(simpleUserRepository, never()).save(any(SimpleUser.class));
    }

    @Test
    void getOrCreateSimpleUser_params_whenUserDoesNotExist_shouldCreateAndReturnNewUser() {
        String newUsername = "anotheruser";
        String newFirstName = "Another";
        String newLastName = "Person";
        String newLocation = "AnotherPlace";

        when(simpleUserRepository.findById(testUserId)).thenReturn(Optional.empty());
        when(simpleUserRepository.save(any(SimpleUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SimpleUser resultUser = simpleUserService.getOrCreateSimpleUser(testUserId, newUsername, newFirstName, newLastName, newLocation);

        assertNotNull(resultUser);
        assertEquals(testUserId, resultUser.getId());
        assertEquals(newUsername, resultUser.getUsername());
        assertEquals(newFirstName, resultUser.getFirstName());
        assertEquals(newLastName, resultUser.getLastName());
        assertEquals(newLocation, resultUser.getLocation());

        verify(simpleUserRepository, times(1)).findById(testUserId);
        verify(simpleUserRepository, times(1)).save(any(SimpleUser.class));
    }

    @Test
    void getOrCreateSimpleUser_params_whenUserIdIsNull_shouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            simpleUserService.getOrCreateSimpleUser(null, "username", "first", "last", "location");
        });
        assertEquals("User ID cannot be null", exception.getMessage());
        verify(simpleUserRepository, never()).findById(any());
        verify(simpleUserRepository, never()).save(any());
    }
}