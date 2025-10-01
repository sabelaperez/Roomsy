package com.roomsy.backend.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.sql.Date;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(unique = true)
    @Email
    private String email;

    @NotNull
    @Size(min = 4, max = 20)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Column(length = 20)
    private String username;

    @Size(min = 4, max = 50)
    @Column(length = 50)
    private String fullName;

    @Column(length = 50)
    private String hashPassword;

    private boolean isActive;

    private Date createdAt;

    private Date updatedAt;

    private Date joinedAt;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;





}

/*  - id (UUID, PK)
  - email (String, unique)
  - username (String)
  - full_name (String)
  - password_hash (String)
  - is_active (Booelan, default: true)
  - created_at (timestamp)
  - updated_at (timestamp)
  - joined_at (timestamp, optional)*/

/*package gal.usc.etse.es.restdemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "authors")
public class Author {
    @Id
    private String id;
    @Column
    private String name;

    public Author() {
    }

    public Author(
            String id,
            String name
    ) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public Author setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Author setName(String name) {
        this.name = name;
        return this;
    }
}
*/