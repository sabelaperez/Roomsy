package com.roomsy.backend.repository;

import com.roomsy.backend.model.CleaningTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CleaningTaskRepository extends JpaRepository<CleaningTask, UUID> {
    Page<CleaningTask> findByGroupId(UUID groupId, Pageable pageable);
}
