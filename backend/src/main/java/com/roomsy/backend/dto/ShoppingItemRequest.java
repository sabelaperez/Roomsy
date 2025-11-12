package com.roomsy.backend.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ShoppingItemRequest {
    @NotNull
    @Size(min = 3, max = 100)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Name can only contain letters, numbers, and spaces")
    private String name;

    @NotNull
    @Min(1)
    private Integer quantity = 1;

    // optional
    private UUID categoryId;

    public ShoppingItemRequest() {}

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

    public UUID getCategoryId() { 
        return categoryId; 
    }
    public void setCategoryId(UUID categoryId) { 
        this.categoryId = categoryId; 
    }
}

