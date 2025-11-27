package com.roomsy.backend.dto;

import com.roomsy.backend.model.Group;
import java.time.LocalDateTime;
import java.util.UUID;

public record GroupResponse(
    UUID id,
    String name,
    String inviteCode,
    int memberCount,
    LocalDateTime createdAt,
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
