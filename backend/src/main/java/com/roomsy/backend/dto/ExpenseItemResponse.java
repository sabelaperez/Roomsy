package com.roomsy.backend.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.roomsy.backend.model.ExpenseItem;
import com.roomsy.backend.model.ExpenseType;
import com.roomsy.backend.model.User;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public record ExpenseItemResponse(
    @JsonView(Views.Basic.class) UUID id,
    @JsonView(Views.Basic.class) UUID groupId,
    @JsonView(Views.Basic.class) UUID ownerId,
    @JsonView(Views.Basic.class) String ownerUsername,
    @JsonView(Views.Basic.class) String name,
    @JsonView(Views.Basic.class) ExpenseType expenseType,
    @JsonView(Views.Basic.class) List<User> usersInvolved,
    @JsonView(Views.Basic.class) Double price,
    @JsonView(Views.Basic.class) Double pricePerPerson,
    @JsonView(Views.Basic.class) Date expenseDate,
    @JsonView(Views.Basic.class) LocalDateTime createdAt,
    @JsonView(Views.Basic.class) LocalDateTime updatedAt
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
