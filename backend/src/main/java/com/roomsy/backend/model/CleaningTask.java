package com.roomsy.backend.model;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotEmpty;

@Entity 
@Table(name = "cleaning_tasks")
public class CleaningTask {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @NotNull
    @Column(nullable = false)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Title can only contain letters, numbers, and spaces")
    private String title;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToMany
    @JoinTable(name = "cleaning_task_users",
        joinColumns = @JoinColumn(name = "cleaning_task_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    @NotEmpty(message = "At least one user must be assigned")
    private List<User> assignedTo = new ArrayList<>();

    private boolean completed = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    // Constructors
    public CleaningTask() {}

    public CleaningTask(Group group, String title, LocalDateTime date, List<User> assignedTo) {
        this.group = group;
        this.title = title;
        this.date = date;
        this.assignedTo = assignedTo;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public Group getGroup() {
        return group;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<User> getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(List<User> assignedTo) {
        this.assignedTo = assignedTo;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }


}
