package com.roomsy.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CategoryRequest {
    @NotNull(message = "Name is required")
    @Size(min = 4, max = 50, message = "Name must be between 4 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Name can only contain letters, numbers, and spaces")
    private String name;

    @NotNull(message = "Color is required")
    private String color;

    public CategoryRequest() {}

    public CategoryRequest(String name, String color) {
        this.name = name;
        this.color = color;
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
}
