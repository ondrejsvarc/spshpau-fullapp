package com.spshpau.userservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "genres")
@Getter
@Setter
@NoArgsConstructor
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ManyToMany(mappedBy = "genres")
    @JsonBackReference
    private Set<ProducerProfile> producerProfiles = new HashSet<>();

    @ManyToMany(mappedBy = "genres")
    @JsonBackReference
    private Set<ArtistProfile> artistProfiles = new HashSet<>();

    public Genre(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre genre = (Genre) o;
        return id != null ? id.equals(genre.id) : name.equals(genre.name);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : name.hashCode();
    }
}
