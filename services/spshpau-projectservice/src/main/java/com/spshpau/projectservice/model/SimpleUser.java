package com.spshpau.projectservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "simpleUsers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleUser {
    @Id
    @Column(nullable = false, unique = true, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column
    private String location;

    // Relationships

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Project> ownedProjects = new HashSet<>();

    @ManyToMany(mappedBy = "collaborators")
    private Set<Project> collaboratingProjects = new HashSet<>();

    @OneToMany(mappedBy = "assignedUser")
    private Set<ProjectTask> assignedTasks = new HashSet<>();
}
