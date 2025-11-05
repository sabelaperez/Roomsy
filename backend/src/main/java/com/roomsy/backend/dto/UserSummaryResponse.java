package com.roomsy.backend.dto;

import com.roomsy.backend.model.User;
import java.util.UUID;

public class UserSummaryResponse {

    private UUID id;
    private String username;
    private String email;
    private UUID groupId;

    public UserSummaryResponse() {}

    public UserSummaryResponse(UUID id, String username, String email,  UUID groupId) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.groupId = groupId;
    }

    public static UserSummaryResponse fromEntity(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getGroup() != null ? user.getGroup().getId() : null
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }
}