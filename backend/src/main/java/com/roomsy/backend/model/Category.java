package com.roomsy.backend.model;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "categories")
public class Category {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @NotNull
    @Column(nullable = false)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Name can only contain letters, numbers, and spaces")
    private String name;

    // Hacer un enum con colores predefinidos
    private String color;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp created_at;

    @UpdateTimestamp
    @Column
    private Timestamp updated_at;

    // Constructors
    public Category() {}

    public Category(Group group, String name, String color) {
        this.group = group;
        this.name = name;
        this.color = color;
    }

    // Getters and Setters


    // Functions
}
