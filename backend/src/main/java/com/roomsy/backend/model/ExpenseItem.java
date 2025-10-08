package com.roomsy.backend.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;

@Entity
@Table(name = "expense_items")
public class ExpenseItem {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany
    private List<User> users_involved = new ArrayList<>();   

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Date expense_date;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column
    private Timestamp updated_at;

    // Constructors´
    public ExpenseItem() {}

    public ExpenseItem(User owner, List<User> users_involved, Double price, Date expense_date) {
        this.owner = owner;
        this.users_involved = users_involved;
        this.price = price;
        this.expense_date = expense_date;
    }

    // Getters and Setters


    // Functions
}
