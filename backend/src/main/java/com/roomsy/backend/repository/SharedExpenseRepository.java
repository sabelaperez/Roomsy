package com.roomsy.backend.repository;

import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.SharedExpense;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SharedExpenseRepository   extends JpaRepository<SharedExpense, UUID> {
    List<SharedExpense> findByGroup(Group group);
    @Transactional
    default boolean deleteByIdReturningBoolean(UUID id) {
        if (existsById(id)) {
            deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}
