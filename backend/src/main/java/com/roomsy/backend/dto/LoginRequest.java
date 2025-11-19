package com.roomsy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request payload")
public record LoginRequest(
        @NotBlank @Email @Schema(description = "User's email address", example = "user@example.com") String email,
        @NotBlank @Schema(description = "User's password", example = "password123") String password
) {}