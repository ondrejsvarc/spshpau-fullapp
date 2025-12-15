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
@Table(name = "artist_profiles")
@Getter
@Setter
@NoArgsConstructor
public class ArtistProfile {

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
            name = "artist_genres",
            joinColumns = @JoinColumn(name = "artist_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @JsonManagedReference
    private Set<Genre> genres = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "artist_skills",
            joinColumns = @JoinColumn(name = "artist_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @JsonManagedReference
    private Set<Skill> skills = new HashSet<>();

    // --- Helper methods ---
    public void addGenre(Genre genre) {
        this.genres.add(genre);
        genre.getArtistProfiles().add(this);
    }
    public void removeGenre(Genre genre) {
        this.genres.remove(genre);
        genre.getArtistProfiles().remove(this);
    }
    public void addSkill(Skill skill) {
        this.skills.add(skill);
        skill.getArtistProfiles().add(this);
    }
    public void removeSkill(Skill skill) {
        this.skills.remove(skill);
        skill.getArtistProfiles().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArtistProfile that = (ArtistProfile) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
