package com.spshpau.userservice.services.impl;

import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.dto.profiledto.ProfileUpdateDto;
import com.spshpau.userservice.dto.profiledto.ProducerProfileDetailDto;
import com.spshpau.userservice.model.*;
import com.spshpau.userservice.repositories.*;
import com.spshpau.userservice.services.ProducerProfileService;
import com.spshpau.userservice.services.exceptions.GenreLimitExceededException;
import com.spshpau.userservice.services.exceptions.GenreNotFoundException;
import com.spshpau.userservice.services.exceptions.ProfileNotFoundException;
import com.spshpau.userservice.services.exceptions.UserNotFoundException;
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
public class ProducerProfileServiceImpl implements ProducerProfileService {

    private final ProducerProfileRepository producerProfileRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;

    private static final int MAX_GENRES = 10;

    private ProducerProfileDetailDto mapEntityToDetailDto(ProducerProfile entity) {
        if (entity == null) return null;
        ProducerProfileDetailDto dto = new ProducerProfileDetailDto();
        dto.setId(entity.getId());
        dto.setAvailability(entity.isAvailability());
        dto.setBio(entity.getBio());
        dto.setExperienceLevel(entity.getExperienceLevel());
        dto.setGenres(entity.getGenres().stream()
                .map(g -> new GenreSummaryDto(g.getId(), g.getName()))
                .collect(Collectors.toSet()));
        return dto;
    }

    private ProducerProfile findProfileByUserIdOrThrow(UUID userId) {
        return producerProfileRepository.findById(userId)
                .map(profile -> {
                    profile.getGenres().size();
                    return profile;
                })
                .orElseThrow(() -> {
                    log.warn("ProducerProfile not found for user ID: {}", userId);
                    return new ProfileNotFoundException("ProducerProfile not found for user ID: " + userId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProducerProfileDetailDto> getProducerProfileByUserId(UUID userId) {
        log.debug("Fetching producer profile for user ID: {}", userId);
        return producerProfileRepository.findById(userId).map(this::mapEntityToDetailDto);
    }

    @Override
    @Transactional
    public ProducerProfileDetailDto createOrUpdateProducerProfile(UUID userId, ProfileUpdateDto profileData) {
        log.info("Creating or updating producer profile for user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        ProducerProfile profile = producerProfileRepository.findById(userId)
                .orElseGet(() -> {
                    ProducerProfile newProfile = new ProducerProfile();
                    newProfile.setUser(user);
                    return newProfile;
                });

        profile.setAvailability(profileData.getAvailability() != null ? profileData.getAvailability() : profile.isAvailability());
        profile.setBio(profileData.getBio() != null ? profileData.getBio() : profile.getBio());
        if (profileData.getExperienceLevel() == null && profile.getId() == null) {
            throw new IllegalArgumentException("Experience level is required for new profile creation.");
        }
        if (profileData.getExperienceLevel() != null) {
            profile.setExperienceLevel(profileData.getExperienceLevel());
        }


        ProducerProfile savedProfile = producerProfileRepository.save(profile);
        return mapEntityToDetailDto(savedProfile);
    }


    @Override
    @Transactional
    public ProducerProfileDetailDto patchProducerProfile(UUID userId, ProfileUpdateDto profileUpdateDto) {
        log.info("Patching producer profile for user ID: {}", userId);
        ProducerProfile profile = findProfileByUserIdOrThrow(userId);

        if (profileUpdateDto.getAvailability() != null) {
            profile.setAvailability(profileUpdateDto.getAvailability());
        }
        if (profileUpdateDto.getBio() != null) {
            profile.setBio(profileUpdateDto.getBio());
        }
        if (profileUpdateDto.getExperienceLevel() != null) {
            profile.setExperienceLevel(profileUpdateDto.getExperienceLevel());
        }

        ProducerProfile savedProfile = producerProfileRepository.save(profile);
        return mapEntityToDetailDto(savedProfile);
    }


    @Override
    @Transactional
    public ProducerProfileDetailDto addGenreToProducerProfile(UUID userId, UUID genreId) {
        log.info("Adding genre {} to producer profile for user ID: {}", genreId, userId);
        ProducerProfile profile = findProfileByUserIdOrThrow(userId);
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new GenreNotFoundException("Genre not found with ID: " + genreId));

        if (profile.getGenres().size() >= MAX_GENRES) {
            throw new GenreLimitExceededException("Cannot add more than " + MAX_GENRES + " genres to ProducerProfile.");
        }
        profile.addGenre(genre);
        ProducerProfile savedProfile = producerProfileRepository.save(profile);
        return mapEntityToDetailDto(savedProfile);
    }

    @Override
    @Transactional
    public ProducerProfileDetailDto removeGenreFromProducerProfile(UUID userId, UUID genreId) {
        log.info("Removing genre {} from producer profile for user ID: {}", genreId, userId);
        ProducerProfile profile = findProfileByUserIdOrThrow(userId);
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new GenreNotFoundException("Genre not found with ID: " + genreId));
        profile.removeGenre(genre);
        ProducerProfile savedProfile = producerProfileRepository.save(profile);
        return mapEntityToDetailDto(savedProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<GenreSummaryDto> getProducerProfileGenres(UUID userId) {
        ProducerProfile profile = findProfileByUserIdOrThrow(userId);
        return profile.getGenres().stream()
                .map(g -> new GenreSummaryDto(g.getId(), g.getName()))
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProducerProfileDetailDto> getProducerProfileByUsername(String username) {
        log.debug("Fetching producer profile for username: {}", username);
        return userRepository.findByUsername(username)
                .flatMap(user -> producerProfileRepository.findById(user.getId()))
                .map(this::mapEntityToDetailDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<GenreSummaryDto> getProducerProfileGenresByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        ProducerProfile profile = findProfileByUserIdOrThrow(user.getId());
        return profile.getGenres().stream()
                .map(g -> new GenreSummaryDto(g.getId(), g.getName()))
                .collect(Collectors.toSet());
    }
}

