package com.roomsy.backend.dto;

import com.roomsy.backend.model.Category;
import java.time.LocalDateTime;
import java.util.UUID;

public class CategoryResponse {
    private UUID id;
    private UUID groupId;
    private String name;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CategoryResponse() {}

    public CategoryResponse(UUID id, UUID groupId, String name, String color,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.groupId = groupId;
        this.name = name;
        this.color = color;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getGroup().getId(),
                category.getName(),
                category.getColor(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
