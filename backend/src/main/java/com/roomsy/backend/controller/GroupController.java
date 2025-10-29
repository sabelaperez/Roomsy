package com.roomsy.backend.controller;

import com.roomsy.backend.dto.*;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.User;
import com.roomsy.backend.service.GroupService;
import com.roomsy.backend.service.UserService;
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
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;

    @Autowired
    public GroupController(GroupService groupService, UserService userService) {
        this.groupService = groupService;
        this.userService = userService;
    }

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

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable UUID groupId) {
        Group group = groupService.getGroupById(groupId);
        return ResponseEntity.ok(GroupResponse.fromEntity(group));
    }

    @PatchMapping("/{groupId}/name")
    public ResponseEntity<GroupResponse> changeGroupName(
            @PathVariable UUID groupId,
            @Valid @RequestBody UpdateGroupNameRequest request) {

        Group updatedGroup = groupService.changeGroupName(groupId, request.getName());
        return ResponseEntity.ok(GroupResponse.fromEntity(updatedGroup));
    }

    @PostMapping("/{groupId}/invite/regenerate")
    public ResponseEntity<InviteCodeResponse> regenerateInviteCode(@PathVariable UUID groupId) {
        String newCode = groupService.regenerateInviteCode(groupId);
        return ResponseEntity.ok(new InviteCodeResponse(newCode));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<GroupResponse> addUserToGroup (
            @PathVariable UUID groupId,
            @Valid @RequestBody AddUserRequest request) {

        Group updatedGroup = groupService.addUserToGroup(groupId, request.getUserId());
        return ResponseEntity.ok(GroupResponse.fromEntity(updatedGroup));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeUserFromGroup(
            @PathVariable UUID groupId,
            @PathVariable UUID userId) {

        Group updatedGroup = groupService.removeUserFromGroup(groupId, userId);

        // If group was deleted (no members left), return 204 No Content
        if (updatedGroup == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<UserSummaryResponse>> getGroupMembers(@PathVariable UUID groupId) {
        List<User> members = groupService.getGroupMembers(groupId);
        List<UserSummaryResponse> response = members.stream()
                .map(UserSummaryResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}/expenses")
    public ResponseEntity<List<ExpenseItemResponse>> getGroupExpenses(@PathVariable UUID groupId) {
        var expenses = groupService.getGroupExpenses(groupId);
        List<ExpenseItemResponse> response = expenses.stream()
                .map(ExpenseItemResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}/shared-expenses")
    public ResponseEntity<List<SharedExpenseResponse>> getGroupSharedExpenses(@PathVariable UUID groupId) {
        var sharedExpenses = groupService.getGroupSharedExpenses(groupId);
        List<SharedExpenseResponse> response = sharedExpenses.stream()
                .map(SharedExpenseResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}/shopping")
    public ResponseEntity<List<ShoppingItemResponse>> getGroupShoppingItems(@PathVariable UUID groupId) {
        var shoppingItems = groupService.getGroupShoppingItems(groupId);
        List<ShoppingItemResponse> response = shoppingItems.stream()
                .map(ShoppingItemResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }


}
