package com.roomsy.backend.repository;

import com.roomsy.backend.model.ExpenseItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, UUID> {
    Page<ExpenseItem> findByGroupId(UUID groupId, Pageable pageable);
}
