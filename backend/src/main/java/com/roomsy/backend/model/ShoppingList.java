package com.roomsy.backend.model;

import java.sql.Timestamp;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;

@Entity
@Table(name = "shopping_lists")
public class ShoppingList {
    /*
    group_id (UUID, PK, FK -> Group)
    price (Decimal, optional)
    updated_at (timestamp)
    items (List of ShoppingItem)
 */

    // Attributes
    // Se un shopping list só pode pertencer a un grupo, igual non debería ser unha clase aparte
    @Id
    @OneToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(nullable = true)
    private Double price;

    @UpdateTimestamp
    @Column
    private Timestamp updated_at;

    // Non sei facer o join, ShoppingItem non ten shoppingList_id
    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private java.util.List<ShoppingItem> items = new java.util.ArrayList<>();

    // Constructors
    public ShoppingList() {}

    public ShoppingList(Group group, Double price) {
        this.group = group;
        this.price = price;
    }

    // Getters and Setters


    // Functions
}