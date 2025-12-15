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
@Table(name = "projectbudgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectBudget {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private float totalAmount;

    // Relationships

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "project_id")
    private Project project;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BudgetExpense> expenses = new HashSet<>();
}
