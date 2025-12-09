package com.roomsy.backend.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for creating a shopping item")
public class ShoppingItemRequest {
    @NotNull
    @Size(min = 3, max = 100)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Name can only contain letters, numbers, and spaces")
    @Schema(description = "Name of the shopping item.", example = "Milk", pattern = "^[a-zA-Z0-9 ]+$", maxLength = 100)
    private String name;

    @NotNull
    @Min(1)
    @Schema(description = "Quantity of the shopping item.", example = "2")
    private Integer quantity = 1;

    @Schema(description = "ID of the category the shopping item belongs to.", example = "3c9e27b0-d3b6-4b7e-a8c1-470f659cb8c9")
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

