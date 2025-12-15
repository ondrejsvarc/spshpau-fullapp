package com.spshpau.userservice.controller;

import com.spshpau.userservice.dto.profiledto.GenreDto;
import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public interface GenreController {

    /**
     * Creates a new Genre.
     * This endpoint is restricted to users with administrative privileges.
     *
     * @param genreDto DTO containing the data for the new genre (e.g., name).
     * @return ResponseEntity containing the created {@link GenreSummaryDto} with a 201 Created status,
     * or an error status if creation fails (e.g., duplicate name).
     * Example Success Response (201 Created):
     * <pre>{@code
     * {
     * "id": "new-genre-uuid",
     * "name": "Reggae"
     * }
     * }</pre>
     */
    ResponseEntity<GenreSummaryDto> addGenre(@RequestBody GenreDto genreDto);

    /**
     * Deletes a Genre by its unique ID.
     * This endpoint is restricted to users with administrative privileges.
     *
     * @param genreId The UUID of the Genre to delete.
     * @return ResponseEntity with a 204 No Content status if successful,
     * or an error status if the genre is not found or cannot be deleted.
     * Example Success Response (204 No Content):
     * (Empty Body)
     */
    ResponseEntity<Void> deleteGenre(@PathVariable UUID genreId);

    /**
     * Retrieves all Genres with support for pagination.
     *
     * @param pageable Pagination information (e.g., page number, size, sort order).
     * @return ResponseEntity containing a Page of {@link GenreSummaryDto},
     * or an error status if retrieval fails.
     * Example Success Response (200 OK):
     * <pre>{@code
     * {
     * "content": [
     * {"id": "genre-uuid-1", "name": "Blues"},
     * {"id": "genre-uuid-2", "name": "Classical"}
     * ],
     * "pageable": {
     * "sort": {"sorted": true, "unsorted": false, "empty": false},
     * "offset": 0,
     * "pageNumber": 0,
     * "pageSize": 20,
     * "paged": true,
     * "unpaged": false
     * },
     * "totalPages": 5,
     * "totalElements": 98,
     * "last": false,
     * "size": 20,
     * "number": 0,
     * "sort": {"sorted": true, "unsorted": false, "empty": false},
     * "numberOfElements": 20,
     * "first": true,
     * "empty": false
     * }
     * }</pre>
     */
    ResponseEntity<Page<GenreSummaryDto>> getAllGenres(Pageable pageable);
}
