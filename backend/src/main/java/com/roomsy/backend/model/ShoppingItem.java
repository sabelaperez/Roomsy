package com.roomsy.backend.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "shopping_items")
public class ShoppingItem {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Unique identifier of the shopping item.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @Schema(description = "The group to which the shopping item belongs.")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = true)
    @Schema(description = "The category to which the shopping item belongs.")
    private Category category;

    @NotNull
    @Size(min = 3, max = 100)
    @Column(nullable = false, length = 100)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Name can only contain letters, numbers, and spaces")
    @Schema(description = "Name of the shopping item.", example = "Milk", pattern = "^[a-zA-Z0-9 ]+$", maxLength = 100)
    private String name;

    @NotNull
    @Column(nullable = false)
    @Schema(description = "Quantity of the shopping item.", example = "2", defaultValue = "1")
    private Integer quantity = 1;

    @CreationTimestamp
    @Column(updatable = false)
    @Schema(description = "Timestamp when the shopping item was created.", example = "2024-06-15T14:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Schema(description = "Timestamp when the shopping item was last updated.", example = "2024-06-15T14:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // Constructors
    public ShoppingItem() {}

    public ShoppingItem(Group group, Category category, String name, Integer quantity) {
        this.group = group;
        this.category = category;
        this.name = name;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
