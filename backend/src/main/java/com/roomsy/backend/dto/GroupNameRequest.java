package com.roomsy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request object for updating a group's name")
public class GroupNameRequest {

    @NotNull(message = "Group name is required")
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Name can only contain letters, numbers, and spaces")
    @Schema(description = "New name for the group", example = "Updated Roomsy Group", required = true)
    private String name;

    // Constructors
    public GroupNameRequest() {}

    public GroupNameRequest(String name) {
        this.name = name;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}