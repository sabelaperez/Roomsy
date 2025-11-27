package com.roomsy.backend.dto;

import com.roomsy.backend.model.Group;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Group response")
public record GroupResponse(
    @Schema(description = "Group id", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    UUID id,
    @Schema(description = "Group name", example = "Roommates")
    String name,
    @Schema(description = "Invite code", example = "INVITE123")
    String inviteCode,
    @Schema(description = "Number of members", example = "4")
    int memberCount,
    @Schema(description = "Creation timestamp", example = "2023-04-01T12:00:00")
    LocalDateTime createdAt,
    @Schema(description = "Last update timestamp", example = "2023-04-02T12:00:00")
    LocalDateTime updatedAt
) {
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
}
