package com.roomsy.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users")
public class User {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotNull
    @Size(min = 4, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Column(nullable = false, length = 20)
    private String username;

    @Size(min = 4, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Full name can only contain letters, numbers, and spaces")
    @Column(length = 50)
    private String fullName;

    @NotNull
    @Column(nullable = false, length = 60)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String hashPassword;

    @NotNull
    @Column(nullable = false)
    private boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id") // Pode non pertencer a ningún grupo
    private Group group;

    // Constructors
    public User() {}

    public User(String email, String username, String fullName, String hashPassword, LocalDateTime joinedAt) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.hashPassword = hashPassword;
        this.joinedAt = joinedAt;
    }

    // Getters and Setters
    public void setGroup(Group group) {
        this.group = group;
    }

    // Functions
}
