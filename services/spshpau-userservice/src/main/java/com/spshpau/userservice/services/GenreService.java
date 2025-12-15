package com.spshpau.userservice.services;

import com.spshpau.userservice.dto.profiledto.GenreDto;
import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.model.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GenreService {
    /**
     * Creates a new genre based on the provided DTO.
     * The genre name must be unique (case-insensitive).
     *
     * @param genreDto A {@link GenreDto} containing the details of the genre to create (e.g., name). Must not be null.
     * @return A {@link GenreSummaryDto} representing the newly created genre.
     * @throws com.spshpau.userservice.services.exceptions.DuplicateException if a genre with the same name already exists.
     */
    GenreSummaryDto createGenre(GenreDto genreDto);

    /**
     * Deletes a genre identified by its unique ID.
     * If the genre is associated with any profiles, the deletion might be restricted
     * or might cascade, depending on repository and database constraints (not specified here, but important consideration).
     *
     * @param genreId The unique identifier of the genre to delete.
     * @throws com.spshpau.userservice.services.exceptions.GenreNotFoundException if no genre with the given ID is found.
     * @throws org.springframework.dao.DataIntegrityViolationException if the genre cannot be deleted due to existing references
     * (e.g., if it's still linked to artist or producer profiles and cascading delete is not configured).
     */
    void deleteGenre(UUID genreId);

    /**
     * Retrieves a paginated list of all available genres.
     *
     * @param pageable Pagination information (e.g., page number, size, sort order). Must not be null.
     * @return A {@link Page} of {@link GenreSummaryDto} objects. Returns an empty page if no genres are found.
     */
    Page<GenreSummaryDto> getAllGenres(Pageable pageable);
}
