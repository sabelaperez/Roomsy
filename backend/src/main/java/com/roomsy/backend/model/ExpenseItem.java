package com.roomsy.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "expense_items")
public class ExpenseItem {

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
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @NotNull
    @ManyToMany
    @JoinTable(name = "expense_item_users",
        joinColumns = @JoinColumn(name = "expense_item_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> users_involved = new ArrayList<>();   

    @NotNull
    @Column(nullable = false)
    private Double price;

    @NotNull
    @Column(nullable = false)
    private Date expense_date;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created_at;

    @UpdateTimestamp
    @Column
    private LocalDateTime updated_at;

    // Constructors´
    public ExpenseItem() {}

    public ExpenseItem(Group group, User owner, List<User> users_involved, Double price, Date expense_date) {
        this.group = group;
        this.owner = owner;
        this.users_involved = users_involved;
        this.price = price;
        this.expense_date = expense_date;
    }

    // Getters and Setters


    // Functions
}
