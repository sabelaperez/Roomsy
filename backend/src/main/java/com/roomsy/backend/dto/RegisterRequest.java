package com.roomsy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Registration request payload")
public record RegisterRequest(
        @NotBlank @Email @Schema(description = "User's email address", example = "user@example.com") String email,
        @NotBlank @Size(min = 4, max = 20) @Schema(description = "Username", example = "john_doe") String username,
        @Size(min = 4, max = 50) @Schema(description = "Full name", example = "John Doe") String fullName,
        @NotBlank @Size(min = 8) @Schema(description = "Password (minimum 8 characters)", example = "password123") String password
) {}