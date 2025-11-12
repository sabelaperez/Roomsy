package com.roomsy.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.roomsy.backend.model.CleaningTask;


public class CleaningTaskResponse {
    private UUID id;
    private UUID groupId;
    private String title;
    private LocalDateTime date;
    private List<UserInvolvedResponse> usersInvolved;
    private boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CleaningTaskResponse() {}

    public CleaningTaskResponse(UUID id, UUID groupId, String title, LocalDateTime date,
                                List<UserInvolvedResponse> usersInvolved, boolean completed,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.groupId = groupId;
        this.title = title;
        this.date = date;
        this.usersInvolved = usersInvolved;
        this.completed = completed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CleaningTaskResponse fromEntity(CleaningTask cleaningTask) {
        List<UserInvolvedResponse> users = cleaningTask.getAssignedTo().stream()
                .map(user -> new UserInvolvedResponse(
                        user.getId(),
                        user.getUsername()
                ))
                .collect(Collectors.toList());

        return new CleaningTaskResponse(
                cleaningTask.getId(),
                cleaningTask.getGroup().getId(),
                cleaningTask.getTitle(),
                cleaningTask.getDate(),
                users,
                cleaningTask.isCompleted(),
                cleaningTask.getCreatedAt(),
                cleaningTask.getUpdatedAt()
        );
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<UserInvolvedResponse> getUsersInvolved() {
        return usersInvolved;
    }

    public void setUsersInvolved(List<UserInvolvedResponse> usersInvolved) {
        this.usersInvolved = usersInvolved;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
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
