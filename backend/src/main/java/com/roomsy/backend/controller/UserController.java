package com.roomsy.backend.controller;

import com.roomsy.backend.dto.UserResponse;
import com.roomsy.backend.model.User;
import com.roomsy.backend.security.CustomUserDetails;
import com.roomsy.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "User", description = "Endpoints for user management")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get user by ID", description = "Retrieves detailed information about a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{user-id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("user-id") UUID userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @Operation(summary = "Delete current user", description = "Deletes the authenticated user's account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteUser(userDetails.getId());
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Delete user by ID", description = "Deletes a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{user-id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable("user-id") UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}