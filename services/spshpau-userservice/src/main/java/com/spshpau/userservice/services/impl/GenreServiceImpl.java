package com.spshpau.userservice.services.impl;

import com.spshpau.userservice.dto.profiledto.GenreDto;
import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.model.Genre;
import com.spshpau.userservice.repositories.GenreRepository;
import com.spshpau.userservice.services.GenreService;
import com.spshpau.userservice.services.exceptions.DuplicateException;
import com.spshpau.userservice.services.exceptions.GenreNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    private GenreSummaryDto mapEntityToSummaryDto(Genre entity) {
        if (entity == null) return null;
        return new GenreSummaryDto(entity.getId(), entity.getName());
    }
    @Override
    @Transactional
    public GenreSummaryDto createGenre(GenreDto genreDto) {
        log.debug("Attempting to create genre with name: {}", genreDto.getName());
        Optional<Genre> existingGenre = genreRepository.findByNameIgnoreCase(genreDto.getName());
        if (existingGenre.isPresent()) {
            log.warn("Genre creation failed: Name '{}' already exists.", genreDto.getName());
            throw new DuplicateException("Genre with name '" + genreDto.getName() + "' already exists.");
        }

        Genre newGenre = new Genre(genreDto.getName());
        Genre savedGenre = genreRepository.save(newGenre);
        log.info("Successfully created genre '{}' with ID: {}", savedGenre.getName(), savedGenre.getId());
        return mapEntityToSummaryDto(savedGenre);
    }

    @Override
    @Transactional
    public void deleteGenre(UUID genreId) {
        log.debug("Attempting to delete genre with ID: {}", genreId);
        if (!genreRepository.existsById(genreId)) {
            log.warn("Genre deletion failed: Genre not found with ID: {}", genreId);
            throw new GenreNotFoundException("Genre not found with ID: " + genreId);
        }
        try {
            genreRepository.deleteById(genreId);
            log.info("Successfully deleted genre with ID: {}", genreId);
        } catch (Exception e) {
            log.error("An unexpected error occurred while deleting genre with ID: {}", genreId, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GenreSummaryDto> getAllGenres(Pageable pageable) {
        log.debug("Fetching genres with pageable: {}", pageable);
        Page<Genre> genrePage = genreRepository.findAll(pageable);
        return genrePage.map(this::mapEntityToSummaryDto);
    }
}
