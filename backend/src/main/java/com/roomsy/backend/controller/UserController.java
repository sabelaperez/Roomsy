package com.roomsy.backend.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.roomsy.backend.dto.GroupResponse;
import com.roomsy.backend.dto.Views;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.User;
import com.roomsy.backend.security.CustomUserDetails;
import com.roomsy.backend.service.GroupService;
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
    private final GroupService groupService;

    @Autowired
    public UserController(UserService userService, GroupService groupService) {
        this.userService = userService;
        this.groupService = groupService;
    }

    @Operation(summary = "Get user by ID", description = "Retrieves detailed information about a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{user-id}")
    @JsonView(Views.Summary.class)
    public ResponseEntity<User> getUserById(@PathVariable("user-id") UUID userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
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

    @Operation(summary = "Add user to a group by invite code", description = "Adds the authenticated user to a group using an invite code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User added to group successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid operation - user already belongs to another group"),
            @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @PostMapping("/{user-id}/join")
    public ResponseEntity<GroupResponse> joinGroupByInviteCode(
            @PathVariable("user-id") UUID userId,
            String inviteCode
    ) {
        Group updatedGroup = groupService.addUserToGroupWithInviteCode(inviteCode, userId);
        return ResponseEntity.ok(GroupResponse.fromEntity(updatedGroup));
    }

    @Operation(summary = "Obtain info about the group the user belongs to", description = "Retrieves information about the group associated with the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group information retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User or group not found")
    })
    @GetMapping("/{user-id}/group")
    public ResponseEntity<GroupResponse> getUserGroupInfo(
        @PathVariable("user-id") UUID userId
    ) {
        User user = userService.getUserById(userId);
        Group group = user.getGroup();
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }
}