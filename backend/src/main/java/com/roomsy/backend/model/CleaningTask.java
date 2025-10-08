package com.roomsy.backend.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity 
@Table(name = "cleaning_tasks")
public class CleaningTask {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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
    private List<User> assignedTo = new ArrayList<>();

    private boolean completed = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime created_at;

    @UpdateTimestamp
    @Column
    private LocalDateTime updated_at;

    // Constructors
    public CleaningTask() {}

    public CleaningTask(Group group, String title, LocalDateTime date) {
        this.group = group;
        this.title = title;
        this.date = date;
    }

    // Getters and Setters


    // Functions

}
