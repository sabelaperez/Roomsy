package com.roomsy.backend.repository;

import com.roomsy.backend.model.SharedExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SharedExpenseRepository   extends JpaRepository<SharedExpense, UUID> {
}
