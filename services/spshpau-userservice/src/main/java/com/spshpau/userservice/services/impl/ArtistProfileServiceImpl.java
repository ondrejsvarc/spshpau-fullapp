package com.spshpau.userservice.services.impl;

import com.spshpau.userservice.dto.profiledto.ArtistProfileDetailDto;
import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.dto.profiledto.ProfileUpdateDto;
import com.spshpau.userservice.dto.profiledto.SkillSummaryDto;
import com.spshpau.userservice.model.*;
import com.spshpau.userservice.repositories.*;
import com.spshpau.userservice.services.ArtistProfileService;
import com.spshpau.userservice.services.exceptions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArtistProfileServiceImpl implements ArtistProfileService {

    private final ArtistProfileRepository artistProfileRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;
    private final SkillRepository skillRepository;

    private static final int MAX_GENRES = 10;
    private static final int MAX_SKILLS = 5;

    private ArtistProfileDetailDto mapEntityToDetailDto(ArtistProfile entity) {
        if (entity == null) return null;
        ArtistProfileDetailDto dto = new ArtistProfileDetailDto();
        dto.setId(entity.getId());
        dto.setAvailability(entity.isAvailability());
        dto.setBio(entity.getBio());
        dto.setExperienceLevel(entity.getExperienceLevel());
        dto.setGenres(entity.getGenres().stream()
                .map(g -> new GenreSummaryDto(g.getId(), g.getName()))
                .collect(Collectors.toSet()));
        dto.setSkills(entity.getSkills().stream()
                .map(s -> new SkillSummaryDto(s.getId(), s.getName()))
                .collect(Collectors.toSet()));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ArtistProfileDetailDto> getArtistProfileByUserId(UUID userId) {
        log.debug("Fetching artist profile for user ID: {}", userId);
        return artistProfileRepository.findById(userId).map(this::mapEntityToDetailDto);
    }

    private ArtistProfile findProfileByUserIdOrThrow(UUID userId) {
        return artistProfileRepository.findById(userId)
                .map(profile -> {
                    profile.getGenres().size();
                    profile.getSkills().size();
                    return profile;
                })
                .orElseThrow(() -> {
                    log.warn("ArtistProfile not found for user ID: {}", userId);
                    return new ProfileNotFoundException("ArtistProfile not found for user ID: " + userId);
                });
    }

    @Override
    @Transactional
    public ArtistProfileDetailDto createOrUpdateArtistProfile(UUID userId, ProfileUpdateDto profileData) {
        log.info("Creating or updating artist profile for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        ArtistProfile profile = artistProfileRepository.findById(userId)
                .orElseGet(() -> {
                    ArtistProfile newProfile = new ArtistProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setAvailability(profileData.getAvailability() != null ? profileData.getAvailability() : profile.isAvailability());
        profile.setBio(profileData.getBio() !=null ? profileData.getBio() : profile.getBio());
        if (profileData.getExperienceLevel() == null && profile.getId() == null) {
            throw new IllegalArgumentException("Experience level is required for new profile creation.");
        }
        if (profileData.getExperienceLevel() != null) {
            profile.setExperienceLevel(profileData.getExperienceLevel());
        }

        ArtistProfile savedProfile = artistProfileRepository.save(profile);
        return mapEntityToDetailDto(savedProfile);
    }


    @Override
    @Transactional
    public ArtistProfileDetailDto patchArtistProfile(UUID userId, ProfileUpdateDto profileUpdateDto) {
        log.info("Patching artist profile for user ID: {}", userId);
        ArtistProfile profile = findProfileByUserIdOrThrow(userId);

        if (profileUpdateDto.getAvailability() != null) {
            profile.setAvailability(profileUpdateDto.getAvailability());
        }
        if (profileUpdateDto.getBio() != null) {
            profile.setBio(profileUpdateDto.getBio());
        }
        if (profileUpdateDto.getExperienceLevel() != null) {
            profile.setExperienceLevel(profileUpdateDto.getExperienceLevel());
        }

        ArtistProfile savedProfile = artistProfileRepository.save(profile);
        return mapEntityToDetailDto(savedProfile);
    }


    @Override
    @Transactional
    public ArtistProfileDetailDto addGenreToArtistProfile(UUID userId, UUID genreId) {
        log.info("Adding genre {} to artist profile for user ID: {}", genreId, userId);
        ArtistProfile profile = findProfileByUserIdOrThrow(userId);
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new GenreNotFoundException("Genre not found with ID: " + genreId));

        if (profile.getGenres().size() >= MAX_GENRES) {
            throw new GenreLimitExceededException("Cannot add more than " + MAX_GENRES + " genres to ArtistProfile.");
        }
        profile.addGenre(genre);
        ArtistProfile savedProfile = artistProfileRepository.save(profile);
        return mapEntityToDetailDto(savedProfile);
    }

    @Override
    @Transactional
    public ArtistProfileDetailDto removeGenreFromArtistProfile(UUID userId, UUID genreId) {
        log.info("Removing genre {} from artist profile for user ID: {}", genreId, userId);
        ArtistProfile profile = findProfileByUserIdOrThrow(userId);
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new GenreNotFoundException("Genre not found with ID: " + genreId));
        profile.removeGenre(genre);
        ArtistProfile savedProfile = artistProfileRepository.save(profile);
        return mapEntityToDetailDto(savedProfile);
    }

    @Override
    @Transactional
    public ArtistProfileDetailDto addSkillToArtistProfile(UUID userId, UUID skillId) {
        log.info("Adding skill {} to artist profile for user ID: {}", skillId, userId);
        ArtistProfile profile = findProfileByUserIdOrThrow(userId);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new SkillNotFoundException("Skill not found with ID: " + skillId));

        if (profile.getSkills().size() >= MAX_SKILLS) {
            throw new SkillLimitExceededException("Cannot add more than " + MAX_SKILLS + " skills to ArtistProfile.");
        }
        profile.addSkill(skill);
        ArtistProfile savedProfile = artistProfileRepository.save(profile);
        return mapEntityToDetailDto(savedProfile);
    }

    @Override
    @Transactional
    public ArtistProfileDetailDto removeSkillFromArtistProfile(UUID userId, UUID skillId) {
        log.info("Removing skill {} from artist profile for user ID: {}", skillId, userId);
        ArtistProfile profile = findProfileByUserIdOrThrow(userId);
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new SkillNotFoundException("Skill not found with ID: " + skillId));
        profile.removeSkill(skill);
        ArtistProfile savedProfile = artistProfileRepository.save(profile);
        return mapEntityToDetailDto(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<GenreSummaryDto> getArtistProfileGenres(UUID userId) {
        ArtistProfile profile = findProfileByUserIdOrThrow(userId);
        return profile.getGenres().stream()
                .map(g -> new GenreSummaryDto(g.getId(), g.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<SkillSummaryDto> getArtistProfileSkills(UUID userId) {
        ArtistProfile profile = findProfileByUserIdOrThrow(userId);
        return profile.getSkills().stream()
                .map(s -> new SkillSummaryDto(s.getId(), s.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ArtistProfileDetailDto> getArtistProfileByUsername(String username) {
        log.debug("Fetching artist profile for username: {}", username);
        return userRepository.findByUsername(username)
                .flatMap(user -> artistProfileRepository.findById(user.getId()))
                .map(this::mapEntityToDetailDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<GenreSummaryDto> getArtistProfileGenresByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        ArtistProfile profile = findProfileByUserIdOrThrow(user.getId());
        return profile.getGenres().stream()
                .map(g -> new GenreSummaryDto(g.getId(), g.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<SkillSummaryDto> getArtistProfileSkillsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        ArtistProfile profile = findProfileByUserIdOrThrow(user.getId());
        return profile.getSkills().stream()
                .map(s -> new SkillSummaryDto(s.getId(), s.getName()))
                .collect(Collectors.toSet());
    }
}
