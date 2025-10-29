package com.roomsy.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "groups")
@Schema(description = "Represents a group of users sharing a living space.", requiredProperties = {"name"})
public class Group {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Unique identifier of the group.", example = "3c9e27b0-d3b6-4b7e-a8c1-470f659cb8c9", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;
    
    @NotNull
    @Column(nullable = false)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Name can only contain letters, numbers, and spaces")
    @Schema(description = "Name of the group.", example = "Roomsy Group", pattern = "^[a-zA-Z0-9 ]+$")
    private String name;

    @CreationTimestamp
    @Column(updatable = false)
    @Schema(description = "Timestamp when the group was created.", example = "2024-01-15T10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    @Schema(description = "Timestamp when the group was last updated.", example = "2024-01-20T15:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Column(unique = true, nullable = false)
    @Schema(description = "Unique invite code for joining the group.", example = "a4mN8V2cR1")
    private String inviteCode;

    @OneToMany(mappedBy = "group", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Schema(description = "List of users who are members of the group.")
    private List<User> members = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    @Schema(description = "List of shopping items associated with the group.")
    private List<ShoppingItem> shoppingItems = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    @Schema(description = "List of expense items associated with the group.")
    private List<ExpenseItem> expenseItems = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    @Schema(description = "List of shared expenses associated with the group.")
    private List<SharedExpense> sharedExpenses = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    @Schema(description = "List of cleaning tasks associated with the group.")
    private List<CleaningTask> cleaningTasks = new ArrayList<>();

    // Constructors
    public Group() {}

    public Group(String name) {
        this.name = name; // Considerar a xeración aleatoria do nome
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public List<CleaningTask> getCleaningTasks() {
        return cleaningTasks;
    }

    public List<ExpenseItem> getExpenseItems() {
        return expenseItems;
    }

    public List<SharedExpense> getSharedExpenses() {
        return sharedExpenses;
    }

    public List<ShoppingItem> getShoppingItems() {
        return shoppingItems;
    }

    // Functions
    public void addMember(User user) {
        if (user == null) return;
        if (!this.members.contains(user)) {
            this.members.add(user);
            user.setGroup(this);
        }      
    }

    public void removeMember(User user) {
        if (user == null) return;
        if (this.members.remove(user)) {
            user.setGroup(null);
        }
    }
}
