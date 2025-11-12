package com.roomsy.backend.controller;

import com.roomsy.backend.dto.ExpenseItemRequest;
import com.roomsy.backend.dto.ExpenseItemResponse;
import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.model.ExpenseItem;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.User;
import com.roomsy.backend.service.ExpenseService;
import com.roomsy.backend.service.GroupService;
import com.roomsy.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping("/groups/{group-id}/expenses")
@Tag(name = "Expenses", description = "Endpoints for managing expenses within groups")
public class ExpenseController {
    
    private final ExpenseService expenseService;
    private final GroupService groupService;
    private final UserService userService;

    @Autowired
    public ExpenseController(ExpenseService expenseService, GroupService groupService, UserService userService) {
        this.expenseService = expenseService;
        this.groupService = groupService;
        this.userService = userService;
    }

    @Operation(summary = "Create a new expense item in a group", description = "Creates a new expense" + 
            " and generates split expenses among the involved users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Expense item and split expenses created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Group or some user not found"),
    })
    @PostMapping
    public ResponseEntity<ExpenseItemResponse> createExpenseItem(
            @PathVariable UUID groupId,
            @Valid @RequestBody ExpenseItemRequest request) throws Exception {
        
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
            @ApiResponse(responseCode = "404", description = "Expense item not found"),
    })
    @DeleteMapping("/items/{expense-item-id}")
    public ResponseEntity<Void> deleteExpenseItem(@PathVariable("expense-item-id") UUID expenseItemId) {
        expenseService.deleteExpenseItem(expenseItemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Pay a shared expense", description = "Deletes a shared expense by its ID" +
            " indicating that it has been paid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Shared expense paid successfully"),
            @ApiResponse(responseCode = "404", description = "Shared expense not found"),
    })
    @DeleteMapping("/shared/{shared-expense-id}")
    public ResponseEntity<Void> paySharedExpense(@PathVariable("shared-expense-id") UUID sharedExpenseId) {
        boolean paid = expenseService.paySharedExpense(sharedExpenseId);
        
        if (paid) {
            return ResponseEntity.noContent().build();
        } else {
            throw new ResourceNotFoundException("SharedExpense not found or could not be paid");
        }
    }
}