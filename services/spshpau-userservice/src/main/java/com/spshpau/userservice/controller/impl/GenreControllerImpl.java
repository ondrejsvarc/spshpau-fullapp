package com.spshpau.userservice.controller.impl;

import com.spshpau.userservice.controller.GenreController;
import com.spshpau.userservice.dto.profiledto.GenreDto;
import com.spshpau.userservice.dto.profiledto.GenreSummaryDto;
import com.spshpau.userservice.model.Genre;
import com.spshpau.userservice.services.GenreService;
import com.spshpau.userservice.services.exceptions.DuplicateException;
import com.spshpau.userservice.services.exceptions.GenreNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreControllerImpl implements GenreController {
    private final GenreService genreService;

    @Override
    @PostMapping("/add")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<GenreSummaryDto> addGenre(@Valid @RequestBody GenreDto genreDto) {
        try {
            GenreSummaryDto createdGenre = genreService.createGenre(genreDto);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(createdGenre.getId())
                    .toUri();
            return ResponseEntity.created(location).body(createdGenre);
        } catch (DuplicateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating genre", ex);
        }
    }

    @Override
    @DeleteMapping("/delete/{genreId}")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<Void> deleteGenre(@PathVariable UUID genreId) {
        try {
            genreService.deleteGenre(genreId);
            return ResponseEntity.noContent().build();
        } catch (GenreNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Genre not found with ID: " + genreId, ex);
        }
        catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting genre", ex);
        }
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<GenreSummaryDto>> getAllGenres(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        try {
            Page<GenreSummaryDto> genrePage = genreService.getAllGenres(pageable);
            return ResponseEntity.ok(genrePage);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving genres", ex);
        }
    }
}
