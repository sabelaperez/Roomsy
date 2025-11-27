package com.roomsy.backend.dto;

import com.roomsy.backend.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Authentication response")
public record AuthResponse(
        @Schema(description = "User ID", example = "3c9e27b0-d3b6-4b7e-a8c1-470f659cb8c9") UUID userId,
        @Schema(description = "User's email", example = "user@example.com") String email,
        @Schema(description = "Username", example = "john_doe") String username,
        @Schema(description = "Full name", example = "John Doe") String fullName,
        @Schema(description = "User role", example = "USER") Role role,
        @Schema(description = "Group ID if user belongs to a group", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890", nullable = true)
        UUID groupId,
        @Schema(description = "Success message", example = "Login successful") String message
) {}