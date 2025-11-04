package com.roomsy.backend.dto;

import com.roomsy.backend.model.Group;
import java.time.LocalDateTime;
import java.util.UUID;

public class GroupResponse {

    private UUID id;
    private String name;
    private String inviteCode;
    private int memberCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GroupResponse() {}

    public GroupResponse(UUID id, String name, String inviteCode, int memberCount,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.inviteCode = inviteCode;
        this.memberCount = memberCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static GroupResponse fromEntity(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getInviteCode(),
                group.getMembers() != null ? group.getMembers().size() : 0,
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
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
}
