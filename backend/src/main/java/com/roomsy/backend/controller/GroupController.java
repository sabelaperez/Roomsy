package com.roomsy.backend.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.roomsy.backend.dto.*;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.User;
import com.roomsy.backend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/groups")
@Tag(name = "Group", description = "Endpoints for managing shared living space groups and their associated resources")
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;
    private final CleaningTaskService cleaningTaskService;
    private final NewsService newsService;
    private final ExpenseService expenseService;

    @Autowired
    public GroupController(GroupService groupService, UserService userService, CleaningTaskService cleaningTaskService,
                           NewsService newsService, ExpenseService expenseService) {
        this.groupService = groupService;
        this.userService = userService;
        this.cleaningTaskService = cleaningTaskService;
        this.newsService = newsService;
        this.expenseService = expenseService;
    }

    @Operation(summary = "Create a new group", description = "Creates a new group with the provided name and" +
            " assigns the creator as the first member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Group created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data - name format or validation error"),
            @ApiResponse(responseCode = "404", description = "Creator user not found")
    })
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
        @Valid @RequestBody GroupRequest request
    ) {
        User creator = userService.getUserById(request.getCreatorId());
        Group group = new Group(request.getName());
        Group savedGroup = groupService.createGroup(group, creator);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GroupResponse.fromEntity(savedGroup));
    }

    @Operation(summary = "Get all groups", description = "Retrieves a list of all existing groups in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getGroups() {
        List<Group> groups = groupService.getGroups();
        List<GroupResponse> response = groups.stream()
                .map(GroupResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get group by ID", description = "Retrieves detailed information about a specific group " +
            "by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group found successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{group-id}")
    public ResponseEntity<GroupResponse> getGroupById(
        @PathVariable("group-id") UUID groupId
    ) {
        Group group = groupService.getGroupById(groupId);
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }

    @Operation(summary = "Update group name", description = "Changes the name of an existing group. Name must contain " +
            "only letters, numbers, and spaces")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group name updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid group name format"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PatchMapping("/{group-id}/name")
    public ResponseEntity<GroupResponse> updateGroupName(
            @PathVariable("group-id") UUID groupId,
            @Valid @RequestBody GroupNameRequest request) {

        Group updatedGroup = groupService.changeGroupName(groupId, request.getName());
        return ResponseEntity.ok(GroupResponse.fromEntity(updatedGroup));
    }

    @Operation(summary = "Delete a group", description = "Permanently deletes a group and all its associated data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Group deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @DeleteMapping("/{group-id}")
    public ResponseEntity<Void> deleteGroup(
        @PathVariable("group-id") UUID groupId
    ) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get invite code", description = "Retrieves the unique invite code for the specified group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invite code retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{group-id}/invite-code")
    public ResponseEntity<InviteCodeResponse> getInviteCode(
        @PathVariable("group-id") UUID groupId
    ) {
        String inviteCode = groupService.getInviteCode(groupId);
        return ResponseEntity.ok(new InviteCodeResponse(inviteCode));
    }

    @Operation(summary = "Regenerate invite code", description = "Generates a new unique invite code for the group, " +
            "invalidating the previous one")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invite code regenerated successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{group-id}/invite-code/regenerate")
    public ResponseEntity<InviteCodeResponse> regenerateInviteCode(
        @PathVariable("group-id") UUID groupId
    ) {
        String newCode = groupService.regenerateInviteCode(groupId);
        return ResponseEntity.ok(new InviteCodeResponse(newCode));
    }

    @Operation(summary = "Get group members", description = "Retrieves a list of all users who are members " +
            "of the specified group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Members retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{group-id}/members")
    @JsonView(Views.Summary.class)
    public ResponseEntity<List<User>> getGroupMembers(
        @PathVariable("group-id") UUID groupId
    ) {
        List<User> members = groupService.getGroupMembers(groupId);

        return ResponseEntity.ok(members);
    }

    @Operation(summary = "Add user to group", description = "Adds an existing user to a group. Users can only belong " +
            "to one group at a time")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User added to group successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid operation - user already belongs to another group"),
            @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @PostMapping("/{group-id}/members/{user-id}")
    public ResponseEntity<GroupResponse> addUserToGroup(
            @PathVariable("group-id") UUID groupId,
            @PathVariable("user-id") UUID userId
    ) {
        Group updatedGroup = groupService.addUserToGroup(groupId, userId);
        return ResponseEntity.ok(GroupResponse.fromEntity(updatedGroup));
    }

    @Operation(summary = "Remove user from group", description = "Removes a user from a group. If the last member is " +
            "removed, the group is automatically deleted")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User removed from group successfully"),
            @ApiResponse(responseCode = "204", description = "User removed and group deleted as no members remain"),
            @ApiResponse(responseCode = "400", description = "Invalid operation - user not in specified group"),
            @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @DeleteMapping("/{group-id}/members/{user-id}")
    public ResponseEntity<GroupResponse> removeUserFromGroup(
            @PathVariable("group-id") UUID groupId,
            @PathVariable("user-id") UUID userId
    ) {
        cleaningTaskService.deleteUser(userId);
        newsService.deleteUser(userId);
        expenseService.deleteUser(userId);
        Group updatedGroup = groupService.removeUserFromGroup(groupId, userId);

        // If group was deleted (no members left), return 204 No Content
        if (updatedGroup == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(GroupResponse.fromEntity(updatedGroup));
    }

    // Request DTOs
    @Schema(description = "Request object for updating a group's name")
    public static class GroupNameRequest {
        @NotNull(message = "Name is required")
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Name can only contain letters, numbers, and spaces")
        @Schema(description = "New name of the group.", example = "Another Group Name", pattern = "^[a-zA-Z0-9 ]+$", maxLength = 50)
        private String name;

        public GroupNameRequest() {}

        public GroupNameRequest(String name) {
                this.name = name;
        }

        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }
    }
}