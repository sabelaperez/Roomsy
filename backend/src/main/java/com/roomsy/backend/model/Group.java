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
    private LocalDateTime created_at;

    @UpdateTimestamp
    @Column
    private LocalDateTime updated_at;

    @Column(unique = true)
    private String invite_code;

    @OneToMany(mappedBy = "group", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<User> members = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ShoppingItem> shoppingItems = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ExpenseItem> expenseItems = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CleaningTask> cleaningTasks = new ArrayList<>();

    // Constructors
    public Group() {}

    public Group(String name, String invite_code) {
        this.name = name;
        this.invite_code = invite_code;
    }

    // Getters and Setters


    // Functions
    public void addMember(User user) {
        if (user == null) return;
        members.add(user);
        user.setGroup(this);
    }

    public void removeMember(User user) {
        if (user == null) return;
        members.remove(user);
        user.setGroup(null);
    }
}
