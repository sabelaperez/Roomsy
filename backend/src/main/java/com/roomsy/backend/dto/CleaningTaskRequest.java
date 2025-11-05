package com.roomsy.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CleaningTaskRequest {
    @NotNull
    @Size(min = 3, max = 100)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Title can only contain letters, numbers, and spaces")
    private String title;

    @NotNull
    private LocalDateTime date;

    @NotEmpty
    private List<UUID> assignedToIds;

    public CleaningTaskRequest() {}

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

    public List<UUID> getAssignedToIds() {
        return assignedToIds;
    }
    public void setAssignedToIds(List<UUID> assignedToIds) {
        this.assignedToIds = assignedToIds;
    }
}
