package com.roomsy.backend.dto;

import com.roomsy.backend.model.SharedExpense;
import java.util.UUID;

public class SharedExpenseResponse {

    private UUID id;
    private UUID groupId;
    private UUID payerId;
    private String payerUsername;
    private UUID payToId;
    private String payToUsername;
    private Double quantity;

    public SharedExpenseResponse() {}

    public SharedExpenseResponse(UUID id, UUID groupId, UUID payerId, String payerUsername,
                                 UUID payToId, String payToUsername, Double quantity) {
        this.id = id;
        this.groupId = groupId;
        this.payerId = payerId;
        this.payerUsername = payerUsername;
        this.payToId = payToId;
        this.payToUsername = payToUsername;
        this.quantity = quantity;
    }

    public static SharedExpenseResponse fromEntity(SharedExpense expense) {
        return new SharedExpenseResponse(
                expense.getId(),
                expense.getGroup().getId(),
                expense.getPayer().getId(),
                expense.getPayer().getUsername(),
                expense.getPayTo().getId(),
                expense.getPayTo().getUsername(),
                expense.getQuantity()
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }
    public UUID getPayerId() { return payerId; }
    public void setPayerId(UUID payerId) { this.payerId = payerId; }
    public String getPayerUsername() { return payerUsername; }
    public void setPayerUsername(String payerUsername) { this.payerUsername = payerUsername; }
    public UUID getPayToId() { return payToId; }
    public void setPayToId(UUID payToId) { this.payToId = payToId; }
    public String getPayToUsername() { return payToUsername; }
    public void setPayToUsername(String payToUsername) { this.payToUsername = payToUsername; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
}
