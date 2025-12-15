package com.spshpau.projectservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "projectbudgetexpanses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BudgetExpense {
    @Id
    @Column(nullable = false, unique = true, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private float amount;

    @Column(nullable = false)
    private Date date;

    @Column(columnDefinition = "TEXT")
    private String comment;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private ProjectBudget budget;
}
