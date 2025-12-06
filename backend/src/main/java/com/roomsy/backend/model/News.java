package com.roomsy.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonView;
import com.roomsy.backend.dto.Views;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "news")
@Schema(description = "Represents a news item related to group activities.")
public class News {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonView(Views.Basic.class)
    @Schema(description = "Unique identifier of the news item.", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotNull
    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne()
    @JoinColumn(name = "group_id", nullable = false)
    @JsonView(Views.Summary.class)
    @Schema(description = "The group to which the news item belongs.")
    private Group group;

    //@ManyToOne(fetch = FetchType.LAZY)
    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    @JsonView(Views.Summary.class)
    @Schema(description = "The user who performed the action leading to the news item.")
    private User actor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonView(Views.Summary.class)
    @Schema(description = "The type of the news item.", example = "MEMBER_ADDED", allowableValues = {"MEMBER_ADDED", "MEMBER_REMOVED", "SHOPPING", "EXPENSE_ADDED", "EXPENSE_PAID", "CLEANING_TASK_ADDED"})
    private NewsType type;

    @NotNull
    @Size(min = 3, max = 100)
    @Column(nullable = false, length = 100)
    @Pattern(regexp = "^[a-zA-Z0-9 _]+$")
    @JsonView(Views.Basic.class)
    @Schema(description = "The name of the news item.", example = "New_member_added", pattern = "^[a-zA-Z0-9 _]+$", maxLength = 100)
    private String name;

    @Size(max = 500)
    @Column(length = 500)
    @Pattern(regexp = "^[a-zA-Z0-9 ,.?!':()-_€]*$")
    @JsonView(Views.Summary.class)
    @Schema(description = "Detailed description of the news item.", example = "User John Doe has been added to the group.", pattern = "^[a-zA-Z0-9 ,.?!':()-_]*$", maxLength = 500)
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @JsonView(Views.Basic.class)
    @Schema(description = "Timestamp when the news item was created.", example = "2024-06-15T14:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    // Constructors
    public News() {}

    public News(Group group, User actor, NewsType type, String name, String description) {
        this.group = group;
        this.actor = actor;
        this.type = type;
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public Group getGroup() {
        return group;
    }

}
