package com.roomsy.backend.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.roomsy.backend.dto.PageResponse;
import com.roomsy.backend.security.CustomUserDetails;
import com.roomsy.backend.util.GroupMembershipValidator;
import com.roomsy.backend.util.patch.JsonPatchOperation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;
import com.roomsy.backend.dto.CleaningTaskRequest;
import com.roomsy.backend.dto.Views;
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

@RestController
@RequestMapping("/groups/{group-id}/cleaning-tasks")
@Tag(name = "Cleaning Tasks", description = "Endpoints for managing cleaning tasks within groups")
public class CleaningTaskController {
    private final CleaningTaskService cleaningTaskService;
    private final GroupService groupService;
    private final UserService userService;
    private final GroupMembershipValidator groupMembershipValidator;

    @Autowired
    public CleaningTaskController(CleaningTaskService cleaningTaskService, GroupService groupsService, UserService userService, GroupMembershipValidator groupMembershipValidator) {
        this.cleaningTaskService = cleaningTaskService;
        this.groupService = groupsService;
        this.userService = userService;
        this.groupMembershipValidator = groupMembershipValidator;
    }

    @Operation(summary = "Create a new cleaning task", description = "Creates a new cleaning task within the specified group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cleaning task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "Group or user not found")
    })
    @PostMapping
    @JsonView(Views.Summary.class)
    public ResponseEntity<CleaningTask> createTask(
            @PathVariable("group-id") UUID groupId,
            @Valid @RequestBody CleaningTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UUID userId = userDetails.getId();
        groupMembershipValidator.verifyGroupMembership(groupId, userId);

        Group group = groupService.getGroupById(groupId);

        List<User> assignees = request.getAssignedToIds().stream()
                .map(userService::getUserById)
                .collect(Collectors.toList());

        CleaningTask task = new CleaningTask(group, request.getTitle(), request.getDate(), assignees);
        CleaningTask saved = cleaningTaskService.createTask(task);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Get a cleaning task by id", description = "Returns the specified cleaning task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cleaning task returned"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{task-id}")
    @JsonView(Views.Summary.class)
    public ResponseEntity<CleaningTask> getTask(
            @PathVariable("task-id") UUID taskId,
            @PathVariable("group-id") UUID groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupMembershipValidator.verifyGroupMembership(groupId, userDetails.getId());

        CleaningTask task = cleaningTaskService.getTask(taskId, groupId);
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Delete a cleaning task", description = "Deletes the specified cleaning task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cleaning task deleted successfully"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{task-id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable("task-id") UUID taskId,
            @PathVariable("group-id") UUID groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupMembershipValidator.verifyGroupMembership(groupId, userDetails.getId());

        cleaningTaskService.deleteTask(taskId, groupId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reassign a cleaning task", description = "Replace assigned users for the specified task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task reassigned successfully"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "Task or some user not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PatchMapping("/{task-id}/assign-to")
    @JsonView(Views.Summary.class)
    public ResponseEntity<CleaningTask> reassignTask(
            @PathVariable("task-id") UUID taskId,
            @PathVariable("group-id") UUID groupId,
            @Valid @RequestBody ReassignRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupMembershipValidator.verifyGroupMembership(groupId, userDetails.getId());

        List<User> newAssignees = request.getAssignedToIds().stream()
                .map(userService::getUserById)
                .collect(Collectors.toList());

        CleaningTask updated = cleaningTaskService.reassignTask(taskId, groupId, newAssignees);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Get the group cleaning tasks", description = "Retrieves all cleaning tasks assigned " +
            "within the group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cleaning tasks retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping()
    @JsonView(Views.Summary.class)
    public ResponseEntity<PageResponse<CleaningTask>> getGroupCleaningTasks(
            @PathVariable("group-id") UUID groupId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDirection,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupMembershipValidator.verifyGroupMembership(groupId, userDetails.getId());

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<CleaningTask> cleaningTasks = cleaningTaskService.getGroupCleaningTasks(groupId, pageable);
        return ResponseEntity.ok(new PageResponse<>(cleaningTasks));
    }

    @Operation(summary = "Update cleaning task", description = "Updates the cleaning task using JSON Patch operations. " +
            "Supports updating 'title', 'date', and 'completed' fields. " +
            "For reassigning users, use the dedicated /assign-to endpoint instead.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cleaning task updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid patch operation"),
            @ApiResponse(responseCode = "403", description = "User is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "Cleaning task not found"),
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "JSON Patch operations to apply",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = JsonPatchOperation.class),
                    examples = {
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Update title",
                                    summary = "Change task title",
                                    value = "[{\"op\": \"replace\", \"path\": \"/title\", \"value\": \"Clean the bathroom\"}]"
                            ),
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Update date",
                                    summary = "Change task date",
                                    value = "[{\"op\": \"replace\", \"path\": \"/date\", \"value\": \"2024-12-15T10:00:00\"}]"
                            ),
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Update completed status",
                                    summary = "Mark task as completed",
                                    value = "[{\"op\": \"replace\", \"path\": \"/completed\", \"value\": true}]"
                            ),
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Multiple operations",
                                    summary = "Update title and mark as completed",
                                    value = "[{\"op\": \"replace\", \"path\": \"/title\", \"value\": \"Clean the kitchen\"}, {\"op\": \"replace\", \"path\": \"/completed\", \"value\": true}]"
                            )
                    }
            )
    )
    @PatchMapping("/{task-id}")
    @JsonView(Views.Summary.class)
    public ResponseEntity<CleaningTask> updateCleaningTask(
            @PathVariable("task-id") UUID taskId,
            @PathVariable("group-id") UUID groupId,
            @RequestBody List<JsonPatchOperation> changes,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupMembershipValidator.verifyGroupMembership(groupId, userDetails.getId());

        CleaningTask updatedTask = cleaningTaskService.updateCleaningTask(taskId, groupId, changes);
        return ResponseEntity.ok(updatedTask);
    }

    // Request DTOs
    @Schema(description = "Request object for updating the assigned users of the cleaning task")
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
}