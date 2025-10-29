package com.roomsy.backend.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "categories", uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "name"}))
@Schema(description = "Represents a category for separating the ShoppingItems within a group.", requiredProperties = {"group", "name", "color"})
public class Category {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Unique identifier of the category.", example = "3c9e27b0-d3b6-4b7e-a8c1-470f659cb8c9", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @Schema(description = "The group to which the category belongs.")
    private Group group;

    @NotNull
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Name can only contain letters, numbers, and spaces")
    @Column(nullable = false, length = 50)
    @Schema(description = "Name of the category.", example = "Groceries", pattern = "^[a-zA-Z0-9 ]+$", maxLength = 50)
    private String name;

    // Facer un enum con colores predefinidos
    @Schema(description = "Color associated with the category.", example = "blue")
    private String color;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(description = "Timestamp when the category was created.", example = "2024-01-15T10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Schema(description = "Timestamp when the category was last updated.", example = "2024-01-20T15:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // Constructors
    public Category() {}

    public Category(Group group, String name, String color) {
        this.group = group;
        this.name = name;
        this.color = color;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
