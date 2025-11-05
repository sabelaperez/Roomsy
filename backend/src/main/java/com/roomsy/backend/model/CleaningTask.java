package com.roomsy.backend.model;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotEmpty;

@Entity 
@Table(name = "cleaning_tasks")
@Schema(description = "Represents a cleaning task assigned to users within a group.", requiredProperties = {"group", "title", "date", "assignedTo"})
public class CleaningTask {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Unique identifier of the cleaning task.", example = "3c9e27b0-d3b6-4b7e-a8c1-470f659cb8c9", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @Schema(description = "The group to which the cleaning task belongs.")
    private Group group;

    @NotNull
    @Size(min = 3, max = 100)
    @Column(nullable = false, length = 100)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Title can only contain letters, numbers, and spaces")
    @Schema(description = "Title of the cleaning task.", example = "Clean the kitchen", pattern = "^[a-zA-Z0-9 ]+$", maxLength = 100)
    private String title;

    @NotNull
    @Column(nullable = false)
    @Schema(description = "Date and time when the cleaning task is scheduled.", example = "2024-07-15T10:00:00")
    private LocalDateTime date;

    @ManyToMany
    @JoinTable(name = "cleaning_task_users",
        joinColumns = @JoinColumn(name = "cleaning_task_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    @NotEmpty(message = "At least one user must be assigned")
    @Schema(description = "List of users assigned to the cleaning task.")
    private List<User> assignedTo = new ArrayList<>();

    @Column(nullable = false)
    @Schema(description = "Indicates whether the cleaning task has been completed.", example = "false", defaultValue = "false")
    private boolean completed = false;

    @CreationTimestamp
    @Column(updatable = false)
    @Schema(description = "Timestamp when the cleaning task was created.", example = "2024-06-01T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Schema(description = "Timestamp when the cleaning task was last updated.", example = "2024-06-01T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
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
