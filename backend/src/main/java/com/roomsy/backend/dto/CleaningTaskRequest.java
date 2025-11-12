package com.roomsy.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CleaningTaskRequest {
    @NotNull
    @Size(min = 3, max = 100)
    @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Title can only contain letters, numbers, and spaces")
    @Schema(description = "Title of the cleaning task", example = "Clean the kitchen", pattern = "^[a-zA-Z0-9 ]+$", minLength = 3, maxLength = 100)
    private String title;

    @NotNull
    @Schema(description = "Date and time of the cleaning task", example = "2024-12-01T14:30:00")
    private LocalDateTime date;

    @NotEmpty
    @Schema(description = "List of user IDs assigned to the cleaning task", example = "[\"3c9e27b0-d3b6-4b7e-a8c1-470f659cb8c9\"]")
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
