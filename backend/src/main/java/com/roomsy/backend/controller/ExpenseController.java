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

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/groups/{groupId}/expenses")
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

    @DeleteMapping("/items/{expenseItemId}")
    public ResponseEntity<Void> deleteExpenseItem(@PathVariable UUID expenseItemId) {
        expenseService.deleteExpenseItem(expenseItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/shared/{sharedExpenseId}")
    public ResponseEntity<Void> paySharedExpense(@PathVariable UUID sharedExpenseId) {
        boolean paid = expenseService.paySharedExpense(sharedExpenseId);
        
        if (paid) {
            return ResponseEntity.noContent().build();
        } else {
            throw new ResourceNotFoundException("SharedExpense not found or could not be paid");
        }
    }
}