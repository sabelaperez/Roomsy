package com.roomsy.backend.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.roomsy.backend.model.ExpenseItem;
import com.roomsy.backend.model.ExpenseType;
import com.roomsy.backend.model.User;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Schema(description = "Expense item response")
public record ExpenseItemResponse(
    @JsonView(Views.Basic.class)
    @Schema(description = "Expense item id", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890") UUID id,
    @JsonView(Views.Basic.class) 
    @Schema(description = "Group id", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890") UUID groupId,
    @JsonView(Views.Basic.class) 
    @Schema(description = "Owner id", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890") UUID ownerId,
    @JsonView(Views.Basic.class) 
    @Schema(description = "Owner username", example = "john_doe") String ownerUsername,
    @JsonView(Views.Basic.class) 
    @Schema(description = "Expense item name", example = "Dinner at restaurant") String name,
    @JsonView(Views.Basic.class) 
    @Schema(description = "Expense type", example = "FOOD", allowableValues = {"GROCERIES", "RENT", "SUPPLIES", "ENTERTAINMENT", "OTHER"}) ExpenseType expenseType,
    @JsonView(Views.Basic.class) 
    @Schema(description = "Users involved in the expense") List<User> usersInvolved,
    @JsonView(Views.Basic.class) 
    @Schema(description = "Total price of the expense", example = "100.0") Double price,
    @JsonView(Views.Basic.class) 
    @Schema(description = "Price per person", example = "25.0") Double pricePerPerson,
    @JsonView(Views.Basic.class) 
    @Schema(description = "Date of the expense", example = "2023-04-01") Date expenseDate,
    @JsonView(Views.Basic.class) 
    @Schema(description = "Creation timestamp", example = "2023-04-01T12:00:00") LocalDateTime createdAt,
    @JsonView(Views.Basic.class) 
    @Schema(description = "Last update timestamp", example = "2023-04-02T12:00:00") LocalDateTime updatedAt
) {
    public static ExpenseItemResponse fromEntity(ExpenseItem expense) {
        double pricePerPerson = expense.getUsersInvolved().isEmpty()
            ? 0.0
            : expense.getPrice() / expense.getUsersInvolved().size();

        return new ExpenseItemResponse(
            expense.getId(),
            expense.getGroup().getId(),
            expense.getOwner().getId(),
            expense.getOwner().getUsername(),
            expense.getName(),
            expense.getExpenseType(),
            expense.getUsersInvolved(),
            expense.getPrice(),
            Math.round(pricePerPerson * 100.0) / 100.0,
            expense.getExpenseDate(),
            expense.getCreatedAt(),
            expense.getUpdatedAt()
        );
    }
}
