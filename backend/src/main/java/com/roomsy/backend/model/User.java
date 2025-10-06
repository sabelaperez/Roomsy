package com.roomsy.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.sql.Timestamp;
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
    @Column(unique = true, nullable = false)
    @Email
    private String email;

    @NotNull
    @Size(min = 4, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Column(length = 20, nullable = false)
    private String username;

    @Size(min = 4, max = 50)
    @Column(length = 50)
    private String fullName;

    @Column(length = 50)
    private String hashPassword;

    private boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column
    private Timestamp updatedAt;

    private Timestamp joinedAt;

    @ManyToOne(optional = true, fetch = FetchType.LAZY) 
    @JoinColumn(name = "group_id", nullable = true) // Pode non pertencer a ningún grupo
    private Group group;

    // Constructors
    public User() {}

    public User(String email, String username, String fullName, String hashPassword) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.hashPassword = hashPassword;
    }

    // Getters and Setters
    public void setGroup(Group group) {
        this.group = group;
    }

    // Functions
}
