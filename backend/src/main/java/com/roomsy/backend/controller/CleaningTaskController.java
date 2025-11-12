package com.roomsy.backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.roomsy.backend.dto.CleaningTaskRequest;
import com.roomsy.backend.dto.CleaningTaskResponse;
import com.roomsy.backend.model.CleaningTask;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.User;
import com.roomsy.backend.service.CleaningTaskService;
import com.roomsy.backend.service.GroupService;
import com.roomsy.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/groups/{group-id}/cleaning-tasks")
@Tag(name = "Cleaning Tasks", description = "Endpoints for managing cleaning tasks within groups")
public class CleaningTaskController {
    private final CleaningTaskService cleaningTaskService;
    private final GroupService groupService;
    private final UserService userService;

    @Autowired
    public CleaningTaskController(CleaningTaskService cleaningTaskService, GroupService groupsService, UserService userService) {
        this.cleaningTaskService = cleaningTaskService;
        this.groupService = groupsService;
        this.userService = userService;
    }

    @Operation(summary = "Create a new cleaning task", description = "Creates a new cleaning task within the specified group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cleaning task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @PostMapping
    public ResponseEntity<CleaningTaskResponse> createTask(
            @PathVariable("group-id") UUID groupId,
            @Valid @RequestBody CleaningTaskRequest request
    ) {
        Group group = groupService.getGroupById(groupId);

        List<User> assignees = request.getAssignedToIds().stream()
                .map(userService::getUserById)
                .collect(Collectors.toList());

        CleaningTask task = new CleaningTask(group, request.getTitle(), request.getDate(), assignees);
        CleaningTask saved = cleaningTaskService.createTask(task);

        return ResponseEntity.status(HttpStatus.CREATED).body(CleaningTaskResponse.fromEntity(saved));
    }

    @Operation(summary = "Get a cleaning task by id", description = "Returns the specified cleaning task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cleaning task returned"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{task-id}")
    public ResponseEntity<CleaningTaskResponse> getTask(
        @PathVariable("task-id") UUID taskId,
        @PathVariable("group-id") UUID groupId
    ) {
        CleaningTask task = cleaningTaskService.getTask(taskId, groupId);
        return ResponseEntity.ok(CleaningTaskResponse.fromEntity(task));
    }

    @Operation(summary = "Delete a cleaning task", description = "Deletes the specified cleaning task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cleaning task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{task-id}")
    public ResponseEntity<Void> deleteTask(
        @PathVariable("task-id") UUID taskId,
        @PathVariable("group-id") UUID groupId
    ) {
        cleaningTaskService.deleteTask(taskId, groupId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reassign a cleaning task", description = "Replace assigned users for the specified task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task reassigned successfully"),
            @ApiResponse(responseCode = "404", description = "Task or some user not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PatchMapping("/{task-id}/assign-to")
    public ResponseEntity<CleaningTaskResponse> reassignTask(
            @PathVariable("task-id") UUID taskId,
            @PathVariable("group-id") UUID groupId,
            @Valid @RequestBody ReassignRequest request
    ) {
        List<User> newAssignees = request.getAssignedToIds().stream()
                .map(userService::getUserById)
                .collect(Collectors.toList());

        CleaningTask updated = cleaningTaskService.reassignTask(taskId, groupId, newAssignees);
        return ResponseEntity.ok(CleaningTaskResponse.fromEntity(updated));
    }

    @Operation(summary = "Set task completed state", description = "Mark task as completed or not completed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PatchMapping("/{task-id}/completed")
    public ResponseEntity<CleaningTaskResponse> setCompleted(
            @PathVariable("task-id") UUID taskId,
            @PathVariable("group-id") UUID groupId,
            @Valid @RequestBody CompletedRequest request
    ) {
        CleaningTask updated = cleaningTaskService.setTaskCompleted(taskId, groupId, request.isCompleted());
        return ResponseEntity.ok(CleaningTaskResponse.fromEntity(updated));
    }

    @Operation(summary = "Change task date", description = "Update the date/time of the task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task date updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PatchMapping("/{task-id}/date")
    public ResponseEntity<CleaningTaskResponse> changeDate(
            @PathVariable("task-id") UUID taskId,
            @PathVariable("group-id") UUID groupId,
            @Valid @RequestBody DateRequest request
    ) {
        CleaningTask updated = cleaningTaskService.changeTaskDate(taskId, groupId, request.getNewDate());
        return ResponseEntity.ok(CleaningTaskResponse.fromEntity(updated));
    }

    @Operation(summary = "Change task title", description = "Update the title of the task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Title updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "400", description = "Invalid title")
    })
    @PatchMapping("/{task-id}/title")
    public ResponseEntity<CleaningTaskResponse> changeTitle(
            @PathVariable("task-id") UUID taskId,
            @PathVariable("group-id") UUID groupId,
            @Valid @RequestBody TitleRequest request
    ) {
        CleaningTask updated = cleaningTaskService.changeTaskTitle(taskId, groupId,request.getTitle());
        return ResponseEntity.ok(CleaningTaskResponse.fromEntity(updated));
    }

    // Request DTOs
    public static class ReassignRequest {
        @NotEmpty
        @Schema(description = "List of user IDs to assign the task to",
                example = "[\"3c9e27b0-d3b6-4b7e-a8c1-470f659cb8c9\", \"660e8400-e29b-41d4-a716-446655440000\"]")
        private List<UUID> assignedToIds;

        public ReassignRequest() {}

        public List<UUID> getAssignedToIds() { 
            return assignedToIds; 
        }
        public void setAssignedToIds(List<UUID> assignedToIds) { 
            this.assignedToIds = assignedToIds; 
        }
    }

    public static class CompletedRequest {
        @NotNull
        @Schema(description = "Indicates whether the task is completed",
                example = "true")
        private Boolean completed;

        public CompletedRequest() {}

        public Boolean isCompleted() {
            return completed; 
        }
        public void setCompleted(Boolean completed) { 
            this.completed = completed; 
        }
    }

    public static class DateRequest {
        @NotNull
        @Schema(description = "New date and time for the task",
                example = "2024-12-01T14:30:00")
        private LocalDateTime newDate;

        public DateRequest() {}

        public LocalDateTime getNewDate() {
            return newDate;
        }
        public void setNewDate(LocalDateTime newDate) {
            this.newDate = newDate;
        }
    }

    public static class TitleRequest {
        @NotNull
        @Size(min = 3, max = 100)
        @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Title can only contain letters, numbers, and spaces")
        @Schema(description = "New title for the task",
                example = "Clean the kitchen",
                minLength = 3,
                maxLength = 100,
                pattern = "^[a-zA-Z0-9 ]+$")
        private String title;

        public TitleRequest() {}

        public String getTitle() { 
            return title; 
        }
        public void setTitle(String title) {
            this.title = title;
        }
    }
}
