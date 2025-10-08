package com.roomsy.backend.repository;

import com.roomsy.backend.model.ExpenseItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExpenseItemRepository extends JpaRepository<ExpenseItem, UUID> {
}
