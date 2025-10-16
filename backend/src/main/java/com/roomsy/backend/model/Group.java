package com.roomsy.backend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "groups")
public class Group {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull
    @Column(nullable = false)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Name can only contain letters, numbers, and spaces")
    private String name;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    @NotNull
    @Column(unique = true)
    private String inviteCode;

    @OneToMany(mappedBy = "group", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<User> members = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ShoppingItem> shoppingItems = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ExpenseItem> expenseItems = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<SharedExpense> sharedExpenses = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CleaningTask> cleaningTasks = new ArrayList<>();

    // Constructors
    public Group() {}

    public Group(String name, String inviteCode, User creator) {
        this.name = name; // Considerar a xeración aleatoria do nome
        this.inviteCode = inviteCode;
        this.members.add(creator);
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
