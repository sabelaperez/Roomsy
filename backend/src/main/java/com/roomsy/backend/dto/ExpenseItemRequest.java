package com.roomsy.backend.dto;

import com.roomsy.backend.model.ExpenseType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ExpenseItemRequest {
    
    @NotNull(message = "Owner ID is required")
    private UUID ownerId;
    
    @NotNull(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Name can only contain letters, numbers, and spaces")
    @Schema(description = "Name of the expense item.", example = "Netflix", pattern = "^[a-zA-Z0-9 ]+$", maxLength = 100)
    private String name;
    
    @NotNull(message = "Expense type is required")
    @Schema(description = "Type of the expense", example = "ENTERTAINMENT", allowableValues = {"GROCERIES", "RENT", "SUPPLIES", "ENTERTAINMENT", "OTHER"})
    private ExpenseType expenseType;
    
    @NotNull(message = "Users involved are required")
    @Size(min = 1, message = "At least one user must be involved")
    @Schema(description = "List of user IDs involved in the expense", example = "[\"3c9e27b0-d3b6-4b7e-a8c1-470f659cb8c9\"]")
    private List<UUID> usersInvolvedIds;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Schema(description = "Price of the expense item", example = "9.99")
    private Double price;
    
    @NotNull(message = "Expense date is required")
    @Schema(description = "Date of the expense", example = "2024-12-01")
    private Date expenseDate;

    // Constructors
    public ExpenseItemRequest() {}

    public ExpenseItemRequest(UUID ownerId, String name, ExpenseType expenseType, 
                             List<UUID> usersInvolvedIds, Double price, Date expenseDate) {
        this.ownerId = ownerId;
        this.name = name;
        this.expenseType = expenseType;
        this.usersInvolvedIds = usersInvolvedIds;
        this.price = price;
        this.expenseDate = expenseDate;
    }

    // Getters and Setters
    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExpenseType getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(ExpenseType expenseType) {
        this.expenseType = expenseType;
    }

    public List<UUID> getUsersInvolvedIds() {
        return usersInvolvedIds;
    }

    public void setUsersInvolvedIds(List<UUID> usersInvolvedIds) {
        this.usersInvolvedIds = usersInvolvedIds;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Date getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(Date expenseDate) {
        this.expenseDate = expenseDate;
    }
}