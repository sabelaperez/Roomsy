package com.roomsy.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "shared_expenses")
public class SharedExpense {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User payer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User payTo;

    @NotNull
    @Column(nullable = false)
    private Double quantity;

    // Constructors
    public SharedExpense() {}

    public SharedExpense(Group group, User payer, User payTo, Double quantity) {
        this.group = group;
        this.payer = payer;
        this.payTo = payTo;
        this.quantity = quantity;
    }

    // Getters and Setters
}
