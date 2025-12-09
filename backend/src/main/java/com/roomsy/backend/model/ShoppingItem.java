package com.roomsy.backend.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonView;
import com.roomsy.backend.dto.Views;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "shopping_items")
@Schema(description = "Represents an item in the shopping list of a group.")
public class ShoppingItem {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonView(Views.Summary.class)
    @Schema(description = "Unique identifier of the shopping item.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotNull
    @JsonBackReference
    @ManyToOne()
    @JoinColumn(name = "group_id", nullable = false)
    @JsonView(Views.Summary.class)
    @Schema(description = "The group to which the shopping item belongs.")
    private Group group;

    @ManyToOne()
    @JoinColumn(name = "category_id", nullable = true)
    @JsonView(Views.Summary.class)
    @Schema(description = "The category to which the shopping item belongs.")
    private Category category;

    @NotNull
    @Size(min = 3, max = 100)
    @Column(nullable = false, length = 100)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Name can only contain letters, numbers, and spaces")
    @JsonView(Views.Summary.class)
    @Schema(description = "Name of the shopping item.", example = "Milk", pattern = "^[a-zA-Z0-9 ]+$", maxLength = 100)
    private String name;

    @NotNull
    @Column(nullable = false)
    @Min(1)
    @JsonView(Views.Summary.class)
    @Schema(description = "Quantity of the shopping item.", example = "2", defaultValue = "1")
    private Integer quantity = 1;

    @CreationTimestamp
    @Column(updatable = false)
    @JsonView(Views.Detailed.class)
    @Schema(description = "Timestamp when the shopping item was created.", example = "2024-06-15T14:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @JsonView(Views.Detailed.class)
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
