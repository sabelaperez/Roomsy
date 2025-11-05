package com.roomsy.backend.dto;

import com.roomsy.backend.model.ShoppingItem;
import java.time.LocalDateTime;
import java.util.UUID;

public class ShoppingItemResponse {

    private UUID id;
    private UUID groupId;
    private String name;
    private int quantity;
    private LocalDateTime createdAt;

    public ShoppingItemResponse() {}

    public static ShoppingItemResponse fromEntity(ShoppingItem item) {
        ShoppingItemResponse response = new ShoppingItemResponse();
        response.id = item.getId();
        response.groupId = item.getGroup().getId();
        response.name = item.getName();
        response.quantity = item.getQuantity();
        response.createdAt = item.getCreatedAt();

        return response;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}