package com.spshpau.userservice.services.impl;

import com.spshpau.userservice.dto.profiledto.ArtistProfileDetailDto;
import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.dto.profiledto.ProfileUpdateDto;
import com.spshpau.userservice.dto.profiledto.SkillSummaryDto;
import com.spshpau.userservice.model.*;
import com.spshpau.userservice.model.enums.ExperienceLevel;
import com.spshpau.userservice.repositories.ArtistProfileRepository;
import com.spshpau.userservice.repositories.GenreRepository;
import com.spshpau.userservice.repositories.SkillRepository;
import com.spshpau.userservice.repositories.UserRepository;
import com.spshpau.userservice.services.exceptions.*;

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
class ArtistProfileServiceImplTest {

    @Mock
    private ArtistProfileRepository artistProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private ArtistProfileServiceImpl artistProfileService;

    private User user;
    private UUID userId;
    private ArtistProfile artistProfile;
    private ProfileUpdateDto profileUpdateDto;
    private Genre genre1, genre2;
    private Skill skill1, skill2;
    private UUID genre1Id, genre2Id;
    private UUID skill1Id, skill2Id;

    private final int MAX_GENRES = 10;
    private final int MAX_SKILLS = 5;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setUsername("testartist");

        artistProfile = new ArtistProfile();
        artistProfile.setId(userId);
        artistProfile.setUser(user);
        artistProfile.setAvailability(true);
        artistProfile.setBio("Test Artist Bio");
        artistProfile.setExperienceLevel(ExperienceLevel.INTERMEDIATE);
        artistProfile.setGenres(new HashSet<>());
        artistProfile.setSkills(new HashSet<>());

        profileUpdateDto = new ProfileUpdateDto();
        profileUpdateDto.setAvailability(false);
        profileUpdateDto.setBio("Updated Artist Bio");
        profileUpdateDto.setExperienceLevel(ExperienceLevel.ADVANCED);

        genre1Id = UUID.randomUUID();
        genre1 = new Genre("Rock");
        genre1.setId(genre1Id);
        genre1.setArtistProfiles(new HashSet<>());


        genre2Id = UUID.randomUUID();
        genre2 = new Genre("Pop");
        genre2.setId(genre2Id);
        genre2.setArtistProfiles(new HashSet<>());


        skill1Id = UUID.randomUUID();
        skill1 = new Skill("Guitar");
        skill1.setId(skill1Id);
        skill1.setArtistProfiles(new HashSet<>());


        skill2Id = UUID.randomUUID();
        skill2 = new Skill("Vocals");
        skill2.setId(skill2Id);
        skill2.setArtistProfiles(new HashSet<>());

    }

    // --- Tests for getArtistProfileByUserId ---
    @Test
    void getArtistProfileByUserId_whenProfileExists_shouldReturnDto() {
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.of(artistProfile));
        Optional<ArtistProfileDetailDto> result = artistProfileService.getArtistProfileByUserId(userId);
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals(artistProfile.getBio(), result.get().getBio());
    }

    @Test
    void getArtistProfileByUserId_whenProfileDoesNotExist_shouldReturnEmptyOptional() {
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.empty());
        Optional<ArtistProfileDetailDto> result = artistProfileService.getArtistProfileByUserId(userId);
        assertFalse(result.isPresent());
    }

    // --- Tests for createOrUpdateArtistProfile ---
    @Test
    void createOrUpdateArtistProfile_newUserProfile_shouldCreateProfile() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.empty());

        when(artistProfileRepository.save(any(ArtistProfile.class))).thenAnswer(invocation -> {
            ArtistProfile profilePassedToSave = invocation.getArgument(0);

            if (profilePassedToSave.getUser() != null) {
                profilePassedToSave.setId(profilePassedToSave.getUser().getId());
            }
            return profilePassedToSave;
        });

        profileUpdateDto.setExperienceLevel(ExperienceLevel.BEGINNER);
        ArtistProfileDetailDto resultDto = artistProfileService.createOrUpdateArtistProfile(userId, profileUpdateDto);

        assertNotNull(resultDto);
        assertEquals(userId, resultDto.getId());
        assertEquals(profileUpdateDto.getBio(), resultDto.getBio());
        assertEquals(profileUpdateDto.getAvailability(), resultDto.isAvailability());
        assertEquals(ExperienceLevel.BEGINNER, resultDto.getExperienceLevel());

        ArgumentCaptor<ArtistProfile> profileCaptor = ArgumentCaptor.forClass(ArtistProfile.class);
        verify(artistProfileRepository).save(profileCaptor.capture());
        ArtistProfile capturedProfileForSave = profileCaptor.getValue();

        assertEquals(user, capturedProfileForSave.getUser());
    }

    @Test
    void createOrUpdateArtistProfile_existingUserProfile_shouldUpdateProfile() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.of(artistProfile));
        when(artistProfileRepository.save(any(ArtistProfile.class))).thenReturn(artistProfile);

        ArtistProfileDetailDto result = artistProfileService.createOrUpdateArtistProfile(userId, profileUpdateDto);

        assertEquals(profileUpdateDto.getBio(), result.getBio());
        assertEquals(profileUpdateDto.getAvailability(), result.isAvailability());
        verify(artistProfileRepository).save(artistProfile);
    }

    @Test
    void createOrUpdateArtistProfile_userNotFound_shouldThrowUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> artistProfileService.createOrUpdateArtistProfile(userId, profileUpdateDto));
    }

    @Test
    void createOrUpdateArtistProfile_newProfileWithoutExperience_shouldThrowIllegalArgumentException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.empty());
        profileUpdateDto.setExperienceLevel(null);
        assertThrows(IllegalArgumentException.class, () -> artistProfileService.createOrUpdateArtistProfile(userId, profileUpdateDto));
    }

    // --- Tests for patchArtistProfile ---
    @Test
    void patchArtistProfile_whenProfileExists_shouldUpdateFields() {
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.of(artistProfile));
        when(artistProfileRepository.save(any(ArtistProfile.class))).thenReturn(artistProfile);

        ProfileUpdateDto patchDto = new ProfileUpdateDto();
        patchDto.setBio("Patched Artist Bio");

        ArtistProfileDetailDto result = artistProfileService.patchArtistProfile(userId, patchDto);
        assertEquals("Patched Artist Bio", result.getBio());
        assertEquals(artistProfile.isAvailability(), result.isAvailability());
        verify(artistProfileRepository).save(artistProfile);
    }

    @Test
    void patchArtistProfile_whenProfileDoesNotExist_shouldThrowProfileNotFoundException() {
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ProfileNotFoundException.class, () -> artistProfileService.patchArtistProfile(userId, profileUpdateDto));
    }

    // --- Tests for addGenreToArtistProfile ---
    @Test
    void addGenreToArtistProfile_valid_shouldAddGenre() {
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.of(artistProfile));
        when(genreRepository.findById(genre1Id)).thenReturn(Optional.of(genre1));
        when(artistProfileRepository.save(any(ArtistProfile.class))).thenReturn(artistProfile);

        ArtistProfileDetailDto result = artistProfileService.addGenreToArtistProfile(userId, genre1Id);
        assertTrue(result.getGenres().stream().anyMatch(g -> g.getId().equals(genre1Id)));
        assertTrue(artistProfile.getGenres().contains(genre1));
        assertTrue(genre1.getArtistProfiles().contains(artistProfile));
    }

    @Test
    void addGenreToArtistProfile_profileNotFound_shouldThrowProfileNotFoundException() {
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ProfileNotFoundException.class, () -> artistProfileService.addGenreToArtistProfile(userId, genre1Id));
    }

    @Test
    void addGenreToArtistProfile_genreNotFound_shouldThrowGenreNotFoundException() {
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.of(artistProfile));
        when(genreRepository.findById(genre1Id)).thenReturn(Optional.empty());
        assertThrows(GenreNotFoundException.class, () -> artistProfileService.addGenreToArtistProfile(userId, genre1Id));
    }

    @Test
    void addGenreToArtistProfile_genreLimitExceeded_shouldThrowGenreLimitExceededException() {
        for (int i = 0; i < MAX_GENRES; i++) {
            artistProfile.addGenre(new Genre("Genre " + i));
        }
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.of(artistProfile));
        when(genreRepository.findById(genre1Id)).thenReturn(Optional.of(genre1));
        assertThrows(GenreLimitExceededException.class, () -> artistProfileService.addGenreToArtistProfile(userId, genre1Id));
    }

    // --- Tests for removeGenreFromArtistProfile ---
    @Test
    void removeGenreFromArtistProfile_valid_shouldRemoveGenre() {
        artistProfile.addGenre(genre1);
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.of(artistProfile));
        when(genreRepository.findById(genre1Id)).thenReturn(Optional.of(genre1));
        when(artistProfileRepository.save(any(ArtistProfile.class))).thenReturn(artistProfile);

        ArtistProfileDetailDto result = artistProfileService.removeGenreFromArtistProfile(userId, genre1Id);
        assertFalse(result.getGenres().stream().anyMatch(g -> g.getId().equals(genre1Id)));
        assertFalse(artistProfile.getGenres().contains(genre1));
        assertFalse(genre1.getArtistProfiles().contains(artistProfile));
    }

    // --- Tests for addSkillToArtistProfile ---
    @Test
    void addSkillToArtistProfile_valid_shouldAddSkill() {
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.of(artistProfile));
        when(skillRepository.findById(skill1Id)).thenReturn(Optional.of(skill1));
        when(artistProfileRepository.save(any(ArtistProfile.class))).thenReturn(artistProfile);

        ArtistProfileDetailDto result = artistProfileService.addSkillToArtistProfile(userId, skill1Id);
        assertTrue(result.getSkills().stream().anyMatch(s -> s.getId().equals(skill1Id)));
        assertTrue(artistProfile.getSkills().contains(skill1));
        assertTrue(skill1.getArtistProfiles().contains(artistProfile));
    }

    @Test
    void addSkillToArtistProfile_skillLimitExceeded_shouldThrowSkillLimitExceededException() {
        for (int i = 0; i < MAX_SKILLS; i++) {
            artistProfile.addSkill(new Skill("Skill " + i));
        }
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.of(artistProfile));
        when(skillRepository.findById(skill1Id)).thenReturn(Optional.of(skill1));
        assertThrows(SkillLimitExceededException.class, () -> artistProfileService.addSkillToArtistProfile(userId, skill1Id));
    }

    // --- Tests for removeSkillFromArtistProfile ---
    @Test
    void removeSkillFromArtistProfile_valid_shouldRemoveSkill() {
        artistProfile.addSkill(skill1);
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.of(artistProfile));
        when(skillRepository.findById(skill1Id)).thenReturn(Optional.of(skill1));
        when(artistProfileRepository.save(any(ArtistProfile.class))).thenReturn(artistProfile);

        ArtistProfileDetailDto result = artistProfileService.removeSkillFromArtistProfile(userId, skill1Id);
        assertFalse(result.getSkills().stream().anyMatch(s -> s.getId().equals(skill1Id)));
        assertFalse(artistProfile.getSkills().contains(skill1));
        assertFalse(skill1.getArtistProfiles().contains(artistProfile));
    }

    // --- Tests for getArtistProfileGenres ---
    @Test
    void getArtistProfileGenres_profileExistsWithGenres_shouldReturnGenreSummaries() {
        artistProfile.addGenre(genre1);
        artistProfile.addGenre(genre2);
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.of(artistProfile));
        Set<GenreSummaryDto> result = artistProfileService.getArtistProfileGenres(userId);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(g -> g.getName().equals("Rock")));
    }

    @Test
    void getArtistProfileGenres_profileNotFound_shouldThrowProfileNotFoundException() {
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ProfileNotFoundException.class, () -> artistProfileService.getArtistProfileGenres(userId));
    }

    // --- Tests for getArtistProfileSkills ---
    @Test
    void getArtistProfileSkills_profileExistsWithSkills_shouldReturnSkillSummaries() {
        artistProfile.addSkill(skill1);
        artistProfile.addSkill(skill2);
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.of(artistProfile));
        Set<SkillSummaryDto> result = artistProfileService.getArtistProfileSkills(userId);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> s.getName().equals("Guitar")));
    }

    // --- Tests for getArtistProfileByUsername ---
    @Test
    void getArtistProfileByUsername_userAndProfileExist_shouldReturnDto() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.of(artistProfile));
        Optional<ArtistProfileDetailDto> result = artistProfileService.getArtistProfileByUsername(user.getUsername());
        assertTrue(result.isPresent());
        assertEquals(artistProfile.getBio(), result.get().getBio());
    }

    @Test
    void getArtistProfileByUsername_userExistsNoProfile_shouldReturnEmpty() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.empty());
        Optional<ArtistProfileDetailDto> result = artistProfileService.getArtistProfileByUsername(user.getUsername());
        assertFalse(result.isPresent());
    }

    @Test
    void getArtistProfileByUsername_userNotFound_shouldReturnEmpty() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        Optional<ArtistProfileDetailDto> result = artistProfileService.getArtistProfileByUsername(user.getUsername());
        assertFalse(result.isPresent());
    }

    // --- Tests for getArtistProfileGenresByUsername ---
    @Test
    void getArtistProfileGenresByUsername_userAndProfileWithGenresExist_shouldReturnGenres() {
        artistProfile.addGenre(genre1);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.of(artistProfile));
        Set<GenreSummaryDto> result = artistProfileService.getArtistProfileGenresByUsername(user.getUsername());
        assertEquals(1, result.size());
        assertEquals("Rock", result.iterator().next().getName());
    }

    @Test
    void getArtistProfileGenresByUsername_userNotFound_shouldThrowUserNotFoundException() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> artistProfileService.getArtistProfileGenresByUsername(user.getUsername()));
    }

    // --- Tests for getArtistProfileSkillsByUsername ---
    @Test
    void getArtistProfileSkillsByUsername_userAndProfileWithSkillsExist_shouldReturnSkills() {
        artistProfile.addSkill(skill1);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(artistProfileRepository.findById(userId)).thenReturn(Optional.of(artistProfile));
        Set<SkillSummaryDto> result = artistProfileService.getArtistProfileSkillsByUsername(user.getUsername());
        assertEquals(1, result.size());
        assertEquals("Guitar", result.iterator().next().getName());
    }
}