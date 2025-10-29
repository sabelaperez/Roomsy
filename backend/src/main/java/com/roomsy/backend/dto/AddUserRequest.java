package com.roomsy.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AddUserRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    public AddUserRequest() {}

    public AddUserRequest(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}