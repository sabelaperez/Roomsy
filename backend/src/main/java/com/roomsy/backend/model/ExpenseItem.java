package com.roomsy.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "expense_items")
public class ExpenseItem {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Unique identifier of the Expense Item.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @Schema(description = "The group to which the expense item belongs.")
    private Group group;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @Schema(description = "The user who created the expense item.")
    private User owner;

    @NotNull
    @Size(min = 3, max = 100)
    @Column(nullable = false, length = 100)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Title can only contain letters, numbers, and spaces")
    @Schema(description = "Name of the expense item.", example = "Groceries", pattern = "^[a-zA-Z0-9 ]+$", maxLength = 100)
    private String name;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Type of the expense.", example = "RENT", allowableValues = {"GROCERIES", "RENT", "SUPPLIES", "ENTERTAINMENT", "OTHER"})
    private ExpenseType expenseType;

    @NotNull
    @ManyToMany
    @JoinTable(name = "expense_item_users",
        joinColumns = @JoinColumn(name = "expense_item_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    @Schema(description = "List of users involved in the expense item.")
    private List<User> usersInvolved = new ArrayList<>();

    @NotNull
    @Column(nullable = false)
    @Schema(description = "Price of the expense item.", example = "100.0")
    private Double price;

    @NotNull
    @Column(nullable = false)
    @Schema(description = "Date of the expense item.", example = "2024-07-15")
    private Date expenseDate;

    @CreationTimestamp
    @Column(updatable = false)
    @Schema(description = "Timestamp when the expense item was created.", example = "2024-06-01T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Schema(description = "Timestamp when the expense item was last updated.", example = "2024-06-01T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // Constructors
    public ExpenseItem() {}

    public ExpenseItem(Group group, User owner, String name, ExpenseType expenseType, List<User> usersInvolved, Double price, Date expenseDate) {
        this.group = group;
        this.owner = owner;
        this.name = name;
        this.expenseType = expenseType;
        this.usersInvolved = usersInvolved;
        this.price = price;
        this.expenseDate = expenseDate;
    }

    // Getters and Setters
    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String nome) {
        this.name = nome;
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

    public Date getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(Date expenseDate) {
        this.expenseDate = expenseDate;
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
