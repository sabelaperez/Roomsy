package com.roomsy.backend.repository;

import com.roomsy.backend.model.CleaningTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CleaningTaskRepository extends JpaRepository<CleaningTask, UUID> {
    List<CleaningTask> findByGroupId(UUID groupId);
}
