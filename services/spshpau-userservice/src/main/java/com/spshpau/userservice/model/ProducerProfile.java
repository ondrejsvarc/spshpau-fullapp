package com.spshpau.userservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.spshpau.userservice.model.enums.ExperienceLevel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "producer_profiles")
@Getter
@Setter
@NoArgsConstructor
public class ProducerProfile {

    @Id
    @Column(name = "user_id")
    private UUID id;

    @Column(nullable = false)
    private boolean availability = false;

    @Column(columnDefinition = "TEXT")
    @Basic(fetch = FetchType.EAGER)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceLevel experienceLevel;

    // --- Relationships ---

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "producer_genres",
            joinColumns = @JoinColumn(name = "producer_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @JsonManagedReference
    private Set<Genre> genres = new HashSet<>();

    // --- Helper methods for managing relationships ---

    public void addGenre(Genre genre) {
        this.genres.add(genre);
        genre.getProducerProfiles().add(this);
    }

    public void removeGenre(Genre genre) {
        this.genres.remove(genre);
        genre.getProducerProfiles().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProducerProfile that = (ProducerProfile) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
