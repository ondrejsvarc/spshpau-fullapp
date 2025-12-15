package com.spshpau.userservice.services.impl;

import com.spshpau.userservice.dto.profiledto.GenreDto;
import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.model.Genre;
import com.spshpau.userservice.repositories.GenreRepository;
import com.spshpau.userservice.services.exceptions.DuplicateException;
import com.spshpau.userservice.services.exceptions.GenreNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceImplTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreServiceImpl genreService;

    private GenreDto genreDto;
    private Genre genre;
    private UUID genreId;

    @BeforeEach
    void setUp() {
        genreId = UUID.randomUUID();

        genreDto = new GenreDto();
        genreDto.setName("Rock");

        genre = new Genre("Rock");
        genre.setId(genreId);
    }

    // --- Tests for createGenre ---
    @Test
    void createGenre_whenNameIsUnique_shouldCreateAndReturnGenreSummaryDto() {
        when(genreRepository.findByNameIgnoreCase(genreDto.getName())).thenReturn(Optional.empty());

        ArgumentCaptor<Genre> genreArgumentCaptor = ArgumentCaptor.forClass(Genre.class);
        when(genreRepository.save(genreArgumentCaptor.capture())).thenReturn(genre);

        GenreSummaryDto result = genreService.createGenre(genreDto);

        assertNotNull(result);
        assertEquals(genre.getId(), result.getId());
        assertEquals(genreDto.getName(), result.getName());

        verify(genreRepository).findByNameIgnoreCase(genreDto.getName());
        verify(genreRepository).save(any(Genre.class));

        Genre capturedGenre = genreArgumentCaptor.getValue();
        assertEquals(genreDto.getName(), capturedGenre.getName());
        assertNull(capturedGenre.getId());
    }

    @Test
    void createGenre_whenNameIsNotUnique_shouldThrowDuplicateException() {
        Genre existingGenre = new Genre("Rock");
        existingGenre.setId(UUID.randomUUID());
        when(genreRepository.findByNameIgnoreCase(genreDto.getName())).thenReturn(Optional.of(existingGenre));

        DuplicateException exception = assertThrows(DuplicateException.class, () -> {
            genreService.createGenre(genreDto);
        });

        assertEquals("Genre with name '" + genreDto.getName() + "' already exists.", exception.getMessage());
        verify(genreRepository).findByNameIgnoreCase(genreDto.getName());
        verify(genreRepository, never()).save(any(Genre.class));
    }

    // --- Tests for deleteGenre ---
    @Test
    void deleteGenre_whenGenreExists_shouldDeleteGenre() {
        when(genreRepository.existsById(genreId)).thenReturn(true);
        doNothing().when(genreRepository).deleteById(genreId);

        assertDoesNotThrow(() -> genreService.deleteGenre(genreId));

        verify(genreRepository).existsById(genreId);
        verify(genreRepository).deleteById(genreId);
    }

    @Test
    void deleteGenre_whenGenreDoesNotExist_shouldThrowGenreNotFoundException() {
        when(genreRepository.existsById(genreId)).thenReturn(false);

        GenreNotFoundException exception = assertThrows(GenreNotFoundException.class, () -> {
            genreService.deleteGenre(genreId);
        });

        assertEquals("Genre not found with ID: " + genreId, exception.getMessage());
        verify(genreRepository).existsById(genreId);
        verify(genreRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deleteGenre_whenRepositoryThrowsUnexpectedException_shouldRethrow() {
        when(genreRepository.existsById(genreId)).thenReturn(true);
        DataIntegrityViolationException dbException = new DataIntegrityViolationException("Database constraint violation");
        doThrow(dbException).when(genreRepository).deleteById(genreId);

        DataIntegrityViolationException thrown = assertThrows(DataIntegrityViolationException.class, () -> {
            genreService.deleteGenre(genreId);
        });
        assertEquals(dbException, thrown);

        verify(genreRepository).existsById(genreId);
        verify(genreRepository).deleteById(genreId);
    }

    // --- Tests for getAllGenres ---
    @Test
    void getAllGenres_whenGenresExist_shouldReturnPageOfGenreSummaryDtos() {
        Pageable pageable = PageRequest.of(0, 10);
        Genre genre1 = new Genre("Pop"); genre1.setId(UUID.randomUUID());
        Genre genre2 = new Genre("Jazz"); genre2.setId(UUID.randomUUID());
        List<Genre> genreList = List.of(genre1, genre2);
        Page<Genre> genrePage = new PageImpl<>(genreList, pageable, genreList.size());

        when(genreRepository.findAll(pageable)).thenReturn(genrePage);

        Page<GenreSummaryDto> result = genreService.getAllGenres(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(genre1.getName(), result.getContent().get(0).getName());
        assertEquals(genre1.getId(), result.getContent().get(0).getId());
        assertEquals(genre2.getName(), result.getContent().get(1).getName());

        verify(genreRepository).findAll(pageable);
    }

    @Test
    void getAllGenres_whenNoGenresExist_shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Genre> emptyGenrePage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(genreRepository.findAll(pageable)).thenReturn(emptyGenrePage);

        Page<GenreSummaryDto> result = genreService.getAllGenres(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(genreRepository).findAll(pageable);
    }
}