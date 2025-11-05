package com.roomsy.backend.controller;

import com.roomsy.backend.dto.*;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.User;
import com.roomsy.backend.service.GroupService;
import com.roomsy.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    public GroupController(GroupService groupService, UserService userService) {
        this.groupService = groupService;
        this.userService = userService;
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
            @Valid @RequestBody CreateGroupRequest request) {

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
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable UUID groupId) {
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
    @PatchMapping("/{groupId}/name")
    public ResponseEntity<GroupResponse> updateGroupName(
            @PathVariable UUID groupId,
            @Valid @RequestBody GroupNameRequest request) {

        Group updatedGroup = groupService.changeGroupName(groupId, request.getName());
        return ResponseEntity.ok(GroupResponse.fromEntity(updatedGroup));
    }

    @Operation(summary = "Delete a group", description = "Permanently deletes a group and all its associated data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Group deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Regenerate invite code", description = "Generates a new unique invite code for the group, " +
            "invalidating the previous one")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Invite code regenerated successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/invite-code/regenerate")
    public ResponseEntity<InviteCodeResponse> regenerateInviteCode(@PathVariable UUID groupId) {
        String newCode = groupService.regenerateInviteCode(groupId);
        return ResponseEntity.ok(new InviteCodeResponse(newCode));
    }

    @Operation(summary = "Get group members", description = "Retrieves a list of all users who are members " +
            "of the specified group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Members retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<UserSummaryResponse>> getGroupMembers(@PathVariable UUID groupId) {
        List<User> members = groupService.getGroupMembers(groupId);
        List<UserSummaryResponse> response = members.stream()
                .map(UserSummaryResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add user to group", description = "Adds an existing user to a group. Users can only belong " +
            "to one group at a time")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User added to group successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid operation - user already belongs to another group"),
            @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupResponse> addUserToGroup(
            @PathVariable UUID groupId,
            @PathVariable UUID userId) {

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
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<GroupResponse> removeUserFromGroup(
            @PathVariable UUID groupId,
            @PathVariable UUID userId) {

        Group updatedGroup = groupService.removeUserFromGroup(groupId, userId);

        // If group was deleted (no members left), return 204 No Content
        if (updatedGroup == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(GroupResponse.fromEntity(updatedGroup));
    }

    @Operation(summary = "Get group expenses", description = "Retrieves all individual expense items associated " +
            "with the group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/expenses")
    public ResponseEntity<List<ExpenseItemResponse>> getGroupExpenses(@PathVariable UUID groupId) {
        var expenses = groupService.getGroupExpenses(groupId);
        List<ExpenseItemResponse> response = expenses.stream()
                .map(ExpenseItemResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get group shared expenses", description = "Retrieves all shared expenses that are split " +
            "among group members")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shared expenses retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/shared-expenses")
    public ResponseEntity<List<SharedExpenseResponse>> getGroupSharedExpenses(@PathVariable UUID groupId) {
        var sharedExpenses = groupService.getGroupSharedExpenses(groupId);
        List<SharedExpenseResponse> response = sharedExpenses.stream()
                .map(SharedExpenseResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get group shopping items", description = "Retrieves all shopping list items for the group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shopping items retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/shopping")
    public ResponseEntity<List<ShoppingItemResponse>> getGroupShoppingItems(@PathVariable UUID groupId) {
        var shoppingItems = groupService.getGroupShoppingItems(groupId);
        List<ShoppingItemResponse> response = shoppingItems.stream()
                .map(ShoppingItemResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get group categories", description = "Retrieves all expense categories configured " +
            "for the group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/categories")
    public ResponseEntity<List<CategoryResponse>> getGroupCategories(@PathVariable UUID groupId) {
        var categories = groupService.getGroupCategories(groupId);
        List<CategoryResponse> response = categories.stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get group cleaning tasks", description = "Retrieves all cleaning tasks assigned " +
            "within the group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cleaning tasks retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}/cleaning-tasks")
    public ResponseEntity<List<CleaningTaskResponse>> getGroupCleaningTasks(@PathVariable UUID groupId) {
        var cleaningTasks = groupService.getGroupCleaningTasks(groupId);
        List<CleaningTaskResponse> response = cleaningTasks.stream()
                .map(CleaningTaskResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
        }
}