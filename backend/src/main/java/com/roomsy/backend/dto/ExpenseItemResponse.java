package com.roomsy.backend.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.roomsy.backend.model.ExpenseItem;
import com.roomsy.backend.model.ExpenseType;
import com.roomsy.backend.model.User;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ExpenseItemResponse {

    private UUID id;
    private UUID groupId;
    private UUID ownerId;
    private String ownerUsername;
    private String name;
    private ExpenseType expenseType;
    @JsonView(Views.Basic.class)
    private List<User> usersInvolved;
    private Double price;
    private Double pricePerPerson;
    private Date expenseDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ExpenseItemResponse() {}

    public ExpenseItemResponse(UUID id, UUID groupId, UUID ownerId, String ownerUsername,
                               String name, ExpenseType expenseType,
                               List<User> usersInvolved, Double price,
                               Double pricePerPerson, Date expenseDate,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.groupId = groupId;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
        this.name = name;
        this.expenseType = expenseType;
        this.usersInvolved = usersInvolved;
        this.price = price;
        this.pricePerPerson = pricePerPerson;
        this.expenseDate = expenseDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

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
                Math.round(pricePerPerson * 100.0) / 100.0, // Round to 2 decimals
                expense.getExpenseDate(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
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

    public List<User> getUsersInvolved() {
        return usersInvolved;
    }

    public void setUsersInvolved(List<User> usersInvolved) {
        this.usersInvolved = usersInvolved;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getPricePerPerson() {
        return pricePerPerson;
    }

    public void setPricePerPerson(Double pricePerPerson) {
        this.pricePerPerson = pricePerPerson;
    }

    public Date getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(Date expenseDate) {
        this.expenseDate = expenseDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
