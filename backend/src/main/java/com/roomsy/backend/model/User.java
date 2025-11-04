package com.roomsy.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Represents a user in the Roomsy application.")
public class User {

    // Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Schema(description = "Unique identifier of the user.", example = "3c9e27b0-d3b6-4b7e-a8c1-470f659cb8c9", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotNull
    @Email
    @Column(unique = true, nullable = false)
    @Schema(description = "Email address of the user.", example = "user@example.com")
    private String email;

    @NotNull
    @Size(min = 4, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Column(nullable = false, length = 20)
    @Schema(description = "Username of the user.", example = "john_doe", pattern = "^[a-zA-Z0-9_]+$")
    private String username;

    @Size(min = 4, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Full name can only contain letters, numbers, and spaces")
    @Column(length = 50)
    @Schema(description = "Full name of the user.", example = "John Doe", pattern = "^[a-zA-Z0-9 ]+$")
    private String fullName;

    @Column(nullable = false, length = 60)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "Hashed password of the user.", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String hashPassword;

    @Column(nullable = false)
    @Schema(description = "Indicates whether the user is active.", example = "true", defaultValue = "true")
    private boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(description = "Timestamp when the user was created.", example = "2024-01-15T10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    @Schema(description = "Timestamp when the user was last updated.", example = "2024-01-20T15:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Schema(description = "Timestamp when the user joined.", example = "2024-01-15T10:05:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime joinedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id") // Pode non pertencer a ningún grupo
    @Schema(description = "The group to which the user belongs.")
    private Group group;

    // Constructors
    public User() {}

    public User(String email, String username, String fullName, String hashPassword) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.hashPassword = hashPassword;
        this.joinedAt = LocalDateTime.now();;
    }

    // Getters and Setters
    public void setGroup(Group group) {
        this.group = group;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public void setHashPassword(String hashPassword) {
        this.hashPassword = hashPassword;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Group getGroup() {
        return group;
    }
}
