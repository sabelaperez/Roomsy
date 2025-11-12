package com.roomsy.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public class CreateGroupRequest {

    @NotNull(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Name can only contain letters, numbers, and spaces")
    @Schema(description = "Name of the group", example = "Group Name", pattern = "^[a-zA-Z0-9 ]+$", maxLength = 50)
    private String name;

    @NotNull(message = "Creator ID is required")
    @Schema(description = "ID of the user creating the group", example = "3c9e27b0-d3b6-4b7e-a8c1-470f659cb8c9")
    private UUID creatorId;

    public CreateGroupRequest() {}

    public CreateGroupRequest(String name, UUID creatorId) {
        this.name = name;
        this.creatorId = creatorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(UUID creatorId) {
        this.creatorId = creatorId;
    }
}
