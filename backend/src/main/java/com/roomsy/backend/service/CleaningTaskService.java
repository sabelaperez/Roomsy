package com.roomsy.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.model.CleaningTask;
import com.roomsy.backend.model.News;
import com.roomsy.backend.model.NewsType;
import com.roomsy.backend.model.User;
import com.roomsy.backend.repository.CleaningTaskRepository;
import com.roomsy.backend.repository.NewsRepository;

@Service
public class CleaningTaskService {
    private final CleaningTaskRepository cleaningTaskRepository;
    private final NewsRepository newsRepository;

    public CleaningTaskService(CleaningTaskRepository cleaningTaskRepository, NewsRepository newsRepository) {
        this.cleaningTaskRepository = cleaningTaskRepository;
        this.newsRepository = newsRepository;
    }

    public CleaningTask getTaskById(@NonNull UUID taskId) throws ResourceNotFoundException {
        return cleaningTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Cleaning task not found with id: " + taskId));
    }

    public CleaningTask createTask(CleaningTask task) {
        // Xerar unha noticia de tipo CLEANING_TASK_ADDED
        String description = "";
        for (User user : task.getAssignedTo()) {
            description += user.getUsername() + " ,";
        }
        description += " are assigned to the task " + task.getTitle() + " on " + task.getDate().toString() + ".";
        News addedNews = new News(task.getGroup(), null, NewsType.CLEANING_TASK_ADDED,
            "Cleaning task " + task.getTitle() + " added to the group", description);

        // Persistir cambios
        newsRepository.save(addedNews);
        return cleaningTaskRepository.save(task);
    }

    public void deleteTask(@NonNull UUID taskId) throws ResourceNotFoundException {
        if(!cleaningTaskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Cleaning task not found with id: " + taskId);
        }
        cleaningTaskRepository.deleteById(taskId);
    }

    public CleaningTask reassignTask(@NonNull UUID taskId, @NonNull List<User> newAssignees) throws ResourceNotFoundException {
        CleaningTask existingTask = getTaskById(taskId);
        existingTask.setAssignedTo(newAssignees);
        return cleaningTaskRepository.save(existingTask);
    }

    public CleaningTask setTaskCompleted(@NonNull UUID taskId, @NonNull boolean completed) throws ResourceNotFoundException {
        CleaningTask existingTask = getTaskById(taskId);
        existingTask.setCompleted(completed);
        return cleaningTaskRepository.save(existingTask);
    }

    public CleaningTask changeTaskDueDate(@NonNull UUID taskId, @NonNull LocalDateTime newDueDate) throws ResourceNotFoundException {
        CleaningTask existingTask = getTaskById(taskId);
        existingTask.setDate(newDueDate);
        return cleaningTaskRepository.save(existingTask);
    }

    public CleaningTask changeTaskTitle(@NonNull UUID taskId, @NonNull String newTitle) throws ResourceNotFoundException {
        CleaningTask existingTask = getTaskById(taskId);
        existingTask.setTitle(newTitle);
        return cleaningTaskRepository.save(existingTask);
    }

}
