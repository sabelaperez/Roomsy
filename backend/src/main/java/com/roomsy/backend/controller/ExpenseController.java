package com.roomsy.backend.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.roomsy.backend.dto.ExpenseItemRequest;
import com.roomsy.backend.dto.ExpenseItemResponse;
import com.roomsy.backend.dto.PageResponse;
import com.roomsy.backend.dto.Views;
import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.model.ExpenseItem;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.SharedExpense;
import com.roomsy.backend.model.User;
import com.roomsy.backend.security.CustomUserDetails;
import com.roomsy.backend.service.ExpenseService;
import com.roomsy.backend.service.GroupService;
import com.roomsy.backend.service.UserService;

import com.roomsy.backend.util.GroupMembershipValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/groups/{group-id}/expenses")
@Tag(name = "Expenses", description = "Endpoints for managing expenses within groups")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final GroupService groupService;
    private final UserService userService;
    private final GroupMembershipValidator groupMembershipValidator;

    @Autowired
    public ExpenseController(ExpenseService expenseService, GroupService groupService, UserService userService, GroupMembershipValidator groupMembershipValidator) {
        this.expenseService = expenseService;
        this.groupService = groupService;
        this.userService = userService;
        this.groupMembershipValidator = groupMembershipValidator;
    }

    @Operation(summary = "Create a new expense item in a group", description = "Creates a new expense" +
            " and generates split expenses among the involved users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Expense item and split expenses created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "Group or some user not found"),
    })
    @PostMapping
    @JsonView(Views.Basic.class)
    public ResponseEntity<ExpenseItemResponse> createExpenseItem(
            @PathVariable("group-id") UUID groupId,
            @Valid @RequestBody ExpenseItemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupMembershipValidator.verifyGroupMembership(groupId, userDetails.getId());

        Group group = groupService.getGroupById(groupId);
        User owner = userService.getUserById(request.getOwnerId());

        List<User> usersInvolved = request.getUsersInvolvedIds().stream()
                .map(userId -> userService.getUserById(userId))
                .collect(Collectors.toList());

        ExpenseItem expenseItem = new ExpenseItem(
                group,
                owner,
                request.getName(),
                request.getExpenseType(),
                usersInvolved,
                request.getPrice(),
                request.getExpenseDate()
        );

        ExpenseItem savedExpenseItem = expenseService.createExpenseItem(expenseItem);
        expenseService.generateSplitExpenses(group, savedExpenseItem);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ExpenseItemResponse.fromEntity(savedExpenseItem));
    }

    @Operation(summary = "Delete an expense item", description = "Deletes an expense item by its ID" +
            " and updates the split expenses")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Expense item deleted successfully"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "Expense item not found"),
    })
    @DeleteMapping("/items/{expense-item-id}")
    public ResponseEntity<Void> deleteExpenseItem(
            @PathVariable("expense-item-id") UUID expenseItemId,
            @PathVariable("group-id") UUID groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupMembershipValidator.verifyGroupMembership(groupId, userDetails.getId());

        expenseService.deleteExpenseItem(expenseItemId, groupId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Pay a shared expense", description = "Deletes a shared expense by its ID" +
            " indicating that it has been paid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Shared expense paid successfully"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "Shared expense not found"),
    })
    @DeleteMapping("/shared/{shared-expense-id}")
    public ResponseEntity<Void> paySharedExpense(
            @PathVariable("shared-expense-id") UUID sharedExpenseId,
            @PathVariable("group-id") UUID groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupMembershipValidator.verifyGroupMembership(groupId, userDetails.getId());

        boolean paid = expenseService.paySharedExpense(sharedExpenseId, groupId);

        if (paid) {
            return ResponseEntity.noContent().build();
        } else {
            throw new ResourceNotFoundException("SharedExpense not found or could not be paid");
        }
    }

    @Operation(summary = "Get group expenses", description = "Retrieves all individual expense items associated " +
            "with the group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping()
    @JsonView(Views.Basic.class)
    public ResponseEntity<PageResponse<ExpenseItemResponse>> getGroupExpenses(
            @PathVariable("group-id") UUID groupId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDirection,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupMembershipValidator.verifyGroupMembership(groupId, userDetails.getId());

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ExpenseItemResponse> expenses = expenseService.getGroupExpenses(groupId, pageable);
        return ResponseEntity.ok(new PageResponse<>(expenses));
    }

    @Operation(summary = "Get group shared expenses", description = "Retrieves all shared expenses that are split " +
            "among group members")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shared expenses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/shared-expenses")
    @JsonView(Views.Summary.class)
    public ResponseEntity<PageResponse<SharedExpense>> getGroupSharedExpenses(
            @PathVariable("group-id") UUID groupId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDirection,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupMembershipValidator.verifyGroupMembership(groupId, userDetails.getId());

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<SharedExpense> sharedExpenses = expenseService.getGroupSharedExpenses(groupId, pageable);
        return ResponseEntity.ok(new PageResponse<>(sharedExpenses));
    }
}