package com.spshpau.userservice.services.impl;

import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.dto.profiledto.ProducerProfileDetailDto;
import com.spshpau.userservice.dto.profiledto.ProfileUpdateDto;
import com.spshpau.userservice.model.Genre;
import com.spshpau.userservice.model.ProducerProfile;
import com.spshpau.userservice.model.User;
import com.spshpau.userservice.model.enums.ExperienceLevel;
import com.spshpau.userservice.repositories.GenreRepository;
import com.spshpau.userservice.repositories.ProducerProfileRepository;
import com.spshpau.userservice.repositories.UserRepository;
import com.spshpau.userservice.services.exceptions.GenreLimitExceededException;
import com.spshpau.userservice.services.exceptions.GenreNotFoundException;
import com.spshpau.userservice.services.exceptions.ProfileNotFoundException;
import com.spshpau.userservice.services.exceptions.UserNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProducerProfileServiceImplTest {

    @Mock
    private ProducerProfileRepository producerProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private ProducerProfileServiceImpl producerProfileService;

    private User user;
    private UUID userId;
    private ProducerProfile producerProfile;
    private ProfileUpdateDto profileUpdateDto;
    private Genre genre1, genre2;
    private UUID genre1Id, genre2Id;

    private final int MAX_GENRES = 10;


    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setUsername("testproducer");

        producerProfile = new ProducerProfile();
        producerProfile.setId(userId);
        producerProfile.setUser(user);
        producerProfile.setAvailability(true);
        producerProfile.setBio("Test Bio");
        producerProfile.setExperienceLevel(ExperienceLevel.INTERMEDIATE);
        producerProfile.setGenres(new HashSet<>());

        profileUpdateDto = new ProfileUpdateDto();
        profileUpdateDto.setAvailability(false);
        profileUpdateDto.setBio("Updated Bio");
        profileUpdateDto.setExperienceLevel(ExperienceLevel.ADVANCED);

        genre1Id = UUID.randomUUID();
        genre1 = new Genre("Rock");
        genre1.setId(genre1Id);
        genre1.setProducerProfiles(new HashSet<>());

        genre2Id = UUID.randomUUID();
        genre2 = new Genre("Pop");
        genre2.setId(genre2Id);
        genre2.setProducerProfiles(new HashSet<>());
    }

    // --- Tests for getProducerProfileByUserId ---
    @Test
    void getProducerProfileByUserId_whenProfileExists_shouldReturnDto() {
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.of(producerProfile));

        Optional<ProducerProfileDetailDto> result = producerProfileService.getProducerProfileByUserId(userId);

        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals(producerProfile.getBio(), result.get().getBio());
        verify(producerProfileRepository).findById(userId);
    }

    @Test
    void getProducerProfileByUserId_whenProfileDoesNotExist_shouldReturnEmptyOptional() {
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<ProducerProfileDetailDto> result = producerProfileService.getProducerProfileByUserId(userId);

        assertFalse(result.isPresent());
        verify(producerProfileRepository).findById(userId);
    }

    // --- Tests for createOrUpdateProducerProfile ---
    @Test
    void createOrUpdateProducerProfile_whenUserExistsAndProfileDoesNotExist_shouldCreateProfile() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.empty());
        when(producerProfileRepository.save(any(ProducerProfile.class))).thenAnswer(invocation -> {
            ProducerProfile savedProfile = invocation.getArgument(0);
            savedProfile.setId(userId);
            return savedProfile;
        });

        profileUpdateDto.setExperienceLevel(ExperienceLevel.BEGINNER);
        ProducerProfileDetailDto result = producerProfileService.createOrUpdateProducerProfile(userId, profileUpdateDto);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(profileUpdateDto.getBio(), result.getBio());
        assertEquals(profileUpdateDto.getAvailability(), result.isAvailability());
        assertEquals(profileUpdateDto.getExperienceLevel(), result.getExperienceLevel());

        ArgumentCaptor<ProducerProfile> profileCaptor = ArgumentCaptor.forClass(ProducerProfile.class);
        verify(producerProfileRepository).save(profileCaptor.capture());
        assertEquals(user, profileCaptor.getValue().getUser());
        assertEquals(ExperienceLevel.BEGINNER, profileCaptor.getValue().getExperienceLevel());
    }

    @Test
    void createOrUpdateProducerProfile_whenUserAndProfileExist_shouldUpdateProfile() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.of(producerProfile));
        when(producerProfileRepository.save(any(ProducerProfile.class))).thenReturn(producerProfile);

        ProducerProfileDetailDto result = producerProfileService.createOrUpdateProducerProfile(userId, profileUpdateDto);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(profileUpdateDto.getBio(), result.getBio());
        assertEquals(profileUpdateDto.getAvailability(), result.isAvailability());
        assertEquals(profileUpdateDto.getExperienceLevel(), result.getExperienceLevel());

        verify(producerProfileRepository).save(producerProfile);
    }

    @Test
    void createOrUpdateProducerProfile_whenUserDoesNotExist_shouldThrowUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            producerProfileService.createOrUpdateProducerProfile(userId, profileUpdateDto);
        });
        verify(producerProfileRepository, never()).save(any());
    }

    @Test
    void createOrUpdateProducerProfile_newProfileWithoutExperience_shouldThrowIllegalArgumentException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.empty());
        profileUpdateDto.setExperienceLevel(null);

        assertThrows(IllegalArgumentException.class, () -> {
            producerProfileService.createOrUpdateProducerProfile(userId, profileUpdateDto);
        });
    }

    // --- Tests for patchProducerProfile ---
    @Test
    void patchProducerProfile_whenProfileExists_shouldUpdateFields() {
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.of(producerProfile));
        when(producerProfileRepository.save(any(ProducerProfile.class))).thenReturn(producerProfile);

        ProfileUpdateDto patchDto = new ProfileUpdateDto();
        patchDto.setBio("Patched Bio");

        ProducerProfileDetailDto result = producerProfileService.patchProducerProfile(userId, patchDto);

        assertNotNull(result);
        assertEquals("Patched Bio", result.getBio());
        assertEquals(producerProfile.isAvailability(), result.isAvailability());
        assertEquals(producerProfile.getExperienceLevel(), result.getExperienceLevel());

        verify(producerProfileRepository).save(producerProfile);
    }

    @Test
    void patchProducerProfile_whenProfileDoesNotExist_shouldThrowProfileNotFoundException() {
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> {
            producerProfileService.patchProducerProfile(userId, profileUpdateDto);
        });
        verify(producerProfileRepository, never()).save(any());
    }

    // --- Tests for addGenreToProducerProfile ---
    @Test
    void addGenreToProducerProfile_valid_shouldAddGenre() {
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.of(producerProfile));
        when(genreRepository.findById(genre1Id)).thenReturn(Optional.of(genre1));
        when(producerProfileRepository.save(any(ProducerProfile.class))).thenReturn(producerProfile);

        ProducerProfileDetailDto result = producerProfileService.addGenreToProducerProfile(userId, genre1Id);

        assertTrue(result.getGenres().stream().anyMatch(g -> g.getId().equals(genre1Id)));
        assertTrue(producerProfile.getGenres().contains(genre1));
        assertTrue(genre1.getProducerProfiles().contains(producerProfile));
        verify(producerProfileRepository).save(producerProfile);
    }

    @Test
    void addGenreToProducerProfile_profileNotFound_shouldThrowProfileNotFoundException() {
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> {
            producerProfileService.addGenreToProducerProfile(userId, genre1Id);
        });
    }

    @Test
    void addGenreToProducerProfile_genreNotFound_shouldThrowGenreNotFoundException() {
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.of(producerProfile));
        when(genreRepository.findById(genre1Id)).thenReturn(Optional.empty());

        assertThrows(GenreNotFoundException.class, () -> {
            producerProfileService.addGenreToProducerProfile(userId, genre1Id);
        });
    }

    @Test
    void addGenreToProducerProfile_genreLimitExceeded_shouldThrowGenreLimitExceededException() {
        for (int i = 0; i < MAX_GENRES; i++) {
            Genre g = new Genre("Genre " + i);
            g.setId(UUID.randomUUID());
            producerProfile.addGenre(g);
        }
        assertEquals(MAX_GENRES, producerProfile.getGenres().size());

        when(producerProfileRepository.findById(userId)).thenReturn(Optional.of(producerProfile));
        when(genreRepository.findById(genre1Id)).thenReturn(Optional.of(genre1));

        assertThrows(GenreLimitExceededException.class, () -> {
            producerProfileService.addGenreToProducerProfile(userId, genre1Id);
        });
    }

    // --- Tests for removeGenreFromProducerProfile ---
    @Test
    void removeGenreFromProducerProfile_valid_shouldRemoveGenre() {
        producerProfile.addGenre(genre1);
        assertTrue(producerProfile.getGenres().contains(genre1));
        assertTrue(genre1.getProducerProfiles().contains(producerProfile));


        when(producerProfileRepository.findById(userId)).thenReturn(Optional.of(producerProfile));
        when(genreRepository.findById(genre1Id)).thenReturn(Optional.of(genre1));
        when(producerProfileRepository.save(any(ProducerProfile.class))).thenReturn(producerProfile);

        ProducerProfileDetailDto result = producerProfileService.removeGenreFromProducerProfile(userId, genre1Id);

        assertFalse(result.getGenres().stream().anyMatch(g -> g.getId().equals(genre1Id)));
        assertFalse(producerProfile.getGenres().contains(genre1));
        assertFalse(genre1.getProducerProfiles().contains(producerProfile));
        verify(producerProfileRepository).save(producerProfile);
    }

    // --- Tests for getProducerProfileGenres ---
    @Test
    void getProducerProfileGenres_whenProfileExistsWithGenres_shouldReturnGenreSummaries() {
        producerProfile.addGenre(genre1);
        producerProfile.addGenre(genre2);
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.of(producerProfile));

        Set<GenreSummaryDto> result = producerProfileService.getProducerProfileGenres(userId);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(g -> g.getName().equals("Rock")));
        assertTrue(result.stream().anyMatch(g -> g.getName().equals("Pop")));
    }

    @Test
    void getProducerProfileGenres_whenProfileNotFound_shouldThrowProfileNotFoundException() {
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ProfileNotFoundException.class, () -> {
            producerProfileService.getProducerProfileGenres(userId);
        });
    }

    // --- Tests for getProducerProfileByUsername ---
    @Test
    void getProducerProfileByUsername_userAndProfileExist_shouldReturnDto() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.of(producerProfile));

        Optional<ProducerProfileDetailDto> result = producerProfileService.getProducerProfileByUsername(user.getUsername());

        assertTrue(result.isPresent());
        assertEquals(producerProfile.getBio(), result.get().getBio());
    }

    @Test
    void getProducerProfileByUsername_userExistsNoProfile_shouldReturnEmpty() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<ProducerProfileDetailDto> result = producerProfileService.getProducerProfileByUsername(user.getUsername());
        assertFalse(result.isPresent());
    }

    @Test
    void getProducerProfileByUsername_userNotFound_shouldReturnEmpty() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        Optional<ProducerProfileDetailDto> result = producerProfileService.getProducerProfileByUsername(user.getUsername());
        assertFalse(result.isPresent());
        verify(producerProfileRepository, never()).findById(any());
    }

    // --- Tests for getProducerProfileGenresByUsername ---
    @Test
    void getProducerProfileGenresByUsername_userAndProfileWithGenresExist_shouldReturnGenres() {
        producerProfile.addGenre(genre1);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.of(producerProfile));

        Set<GenreSummaryDto> result = producerProfileService.getProducerProfileGenresByUsername(user.getUsername());
        assertEquals(1, result.size());
        assertEquals("Rock", result.iterator().next().getName());
    }

    @Test
    void getProducerProfileGenresByUsername_userNotFound_shouldThrowUserNotFoundException() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> {
            producerProfileService.getProducerProfileGenresByUsername(user.getUsername());
        });
    }

    @Test
    void getProducerProfileGenresByUsername_userFoundButProfileNotFound_shouldThrowProfileNotFoundException() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(producerProfileRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class, () -> {
            producerProfileService.getProducerProfileGenresByUsername(user.getUsername());
        });
    }
}