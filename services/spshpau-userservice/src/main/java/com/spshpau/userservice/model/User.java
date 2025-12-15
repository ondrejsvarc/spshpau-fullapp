package com.spshpau.userservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column
    private String location;

    @Column(nullable = false)
    private boolean active = true; // For admin deactivation (ban)

    // --- Relationships ---

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_blocks",
            joinColumns = @JoinColumn(name = "blocker_id"),
            inverseJoinColumns = @JoinColumn(name = "blocked_id")
    )
    @JsonIgnore
    @ToString.Exclude
    private Set<User> blockedUsers = new HashSet<>();

    @OneToOne(mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    @JsonManagedReference
    private ProducerProfile producerProfile;

    @OneToOne(mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    @JsonManagedReference
    private ArtistProfile artistProfile;

    // --- Helper methods ---

    public void setProducerProfile(ProducerProfile producerProfile) {
        if (producerProfile == null) {
            if (this.producerProfile != null) {
                this.producerProfile.setUser(null);
            }
        } else {
            producerProfile.setUser(this);
        }
        this.producerProfile = producerProfile;
    }

    public void setArtistProfile(ArtistProfile artistProfile) {
        if (artistProfile == null) {
            if (this.artistProfile != null) {
                this.artistProfile.setUser(null);
            }
        } else {
            artistProfile.setUser(this);
        }
        this.artistProfile = artistProfile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}