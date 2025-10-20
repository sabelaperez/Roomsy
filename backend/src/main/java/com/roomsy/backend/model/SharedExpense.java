package com.roomsy.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Entity
@Table(name = "shared_expenses")
public class SharedExpense {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pay_to_id", nullable = false)
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


    public UUID getId() {
        return id;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public User getPayer() {
        return payer;
    }

    public void setPayer(User payer) {
        this.payer = payer;
    }

    public User getPayTo() {
        return payTo;
    }

    public void setPayTo(User payTo) {
        this.payTo = payTo;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }
}
