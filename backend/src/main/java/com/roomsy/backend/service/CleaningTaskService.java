package com.roomsy.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.roomsy.backend.exception.ForbiddenException;
import com.roomsy.backend.model.*;
import com.roomsy.backend.util.patch.JsonPatch;
import com.roomsy.backend.util.patch.JsonPatchOperation;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.repository.CleaningTaskRepository;
import com.roomsy.backend.repository.NewsRepository;

import jakarta.annotation.Nonnull;

@Service
public class CleaningTaskService {
    private final CleaningTaskRepository cleaningTaskRepository;
    private final NewsRepository newsRepository;
    private final JsonMapper jsonMapper;

    public CleaningTaskService(CleaningTaskRepository cleaningTaskRepository, NewsRepository newsRepository, JsonMapper jsonMapper) {
        this.cleaningTaskRepository = cleaningTaskRepository;
        this.newsRepository = newsRepository;
        this.jsonMapper = jsonMapper;
    }

    public CleaningTask getTask(@NonNull UUID taskId, @NonNull UUID groupId) throws ResourceNotFoundException {
        CleaningTask task = cleaningTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Cleaning task not found with id: " + taskId));
        if (!task.getGroup().getId().equals(groupId)) {
            throw new ResourceNotFoundException("Cleaning task not found in the specified group");
        }
        return task;
    }

    public boolean existTask(@NonNull UUID taskId, @NonNull UUID groupId) throws ResourceNotFoundException {
        CleaningTask task = cleaningTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Cleaning task not found with id: " + taskId));
        if (!task.getGroup().getId().equals(groupId)) {
            throw new ResourceNotFoundException("Cleaning task not found in the specified group");
        }
        return true;
    }

    @Transactional
    public CleaningTask createTask(CleaningTask task) {
        for (User user : task.getAssignedTo()) {
            if(!task.getGroup().getMembers().contains(user)) {
                throw new ForbiddenException("User " + user.getUsername() + " is not a member of this group");
            }
        }

        String description = "";
        for (User user : task.getAssignedTo()) {
            description += user.getUsername() + " ,";
        }
        description += " are assigned to the task " + task.getTitle() + " on " + task.getDate().toString() + "."; 
        News addedNews = new News(task.getGroup(), task.getAssignedTo().getFirst(), NewsType.CLEANING_TASK_ADDED,
            "Cleaning task " + task.getTitle() + " added to the group", description);

        newsRepository.save(addedNews);
        return cleaningTaskRepository.save(task);
    }

    @Transactional
    public void deleteTask(@NonNull UUID taskId, @NonNull UUID groupId) throws ResourceNotFoundException {
        if(existTask(taskId, groupId)) {
            cleaningTaskRepository.deleteById(taskId);
        }
    }

    public CleaningTask reassignTask(@NonNull UUID taskId, @Nonnull UUID groupId, @NonNull List<User> newAssignees) throws ResourceNotFoundException {
        CleaningTask existingTask = getTask(taskId, groupId);
        existingTask.setAssignedTo(newAssignees);
        return cleaningTaskRepository.save(existingTask);
    }

    public Page<CleaningTask> getGroupCleaningTasks(@NonNull UUID groupId, @NonNull Pageable pageable) throws ResourceNotFoundException {
        return cleaningTaskRepository.findByGroupId(groupId, pageable);
    }

    @Transactional
    public void deleteUser (@NonNull UUID userId) {
        cleaningTaskRepository.findByAssignedToId(userId).forEach(task -> {
            task.getAssignedTo().removeIf(user -> user.getId().equals(userId));
            cleaningTaskRepository.save(task);
        });
    }
      
    @Transactional
    public CleaningTask updateCleaningTask(@NonNull UUID taskId, @NonNull UUID groupId, List<JsonPatchOperation> changes)
            throws IllegalArgumentException {
        CleaningTask cleaningTask = getTask(taskId, groupId);

        JsonNode taskNode = jsonMapper.convertValue(cleaningTask, JsonNode.class);

        JsonNode patchedNode = JsonPatch.apply(changes, taskNode);

        CleaningTask updatedCleaningTask = jsonMapper.convertValue(patchedNode, CleaningTask.class);

        updatedCleaningTask.setGroup(cleaningTask.getGroup());

        return cleaningTaskRepository.save(updatedCleaningTask);
    }
}
