package com.roomsy.backend.dto;

import com.roomsy.backend.model.ExpenseItem;
import com.roomsy.backend.model.ExpenseType;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ExpenseItemResponse {
    
    private UUID id;
    private UUID groupId;
    private UUID ownerId;
    private String ownerName;
    private String name;
    private ExpenseType expenseType;
    private List<UUID> usersInvolvedIds;
    private Double price;
    private Date expenseDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public ExpenseItemResponse() {}

    public ExpenseItemResponse(UUID id, UUID groupId, UUID ownerId, String ownerName, 
                              String name, ExpenseType expenseType, List<UUID> usersInvolvedIds, 
                              Double price, Date expenseDate, LocalDateTime createdAt, 
                              LocalDateTime updatedAt) {
        this.id = id;
        this.groupId = groupId;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.name = name;
        this.expenseType = expenseType;
        this.usersInvolvedIds = usersInvolvedIds;
        this.price = price;
        this.expenseDate = expenseDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Factory method
    public static ExpenseItemResponse fromEntity(ExpenseItem expenseItem) {
        return new ExpenseItemResponse(
                expenseItem.getId(),
                expenseItem.getGroup().getId(),
                expenseItem.getOwner().getId(),
                expenseItem.getOwner().getFullName(),
                expenseItem.getName(),
                expenseItem.getExpenseType(),
                expenseItem.getUsersInvolved().stream()
                        .map(user -> user.getId())
                        .collect(Collectors.toList()),
                expenseItem.getPrice(),
                expenseItem.getExpenseDate(),
                expenseItem.getCreatedAt(),
                expenseItem.getUpdatedAt()
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

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
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
