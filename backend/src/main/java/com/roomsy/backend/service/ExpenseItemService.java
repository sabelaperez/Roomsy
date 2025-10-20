package com.roomsy.backend.service;

import com.roomsy.backend.model.ExpenseItem;
import com.roomsy.backend.repository.ExpenseItemRepository;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExpenseItemService {

    private ExpenseItemRepository expenseItemRepository;

    @Autowired
    public ExpenseItemService(ExpenseItemRepository expenseItemRepository) {
        this.expenseItemRepository = expenseItemRepository;
    }

    @Transactional
    public ExpenseItem createExpenseItem(@NonNull ExpenseItem expenseItem) throws Exception {
        return expenseItemRepository.save(expenseItem);
    }

    @Transactional
    public void deleteExpenseItem(@NonNull UUID id) throws Exception {
        if(!expenseItemRepository.existsById(id)) {
            throw new IllegalArgumentException("ExpenseItem with that id does not exist");
        }

        expenseItemRepository.deleteById(id);
        // TODO: gasto compensatorio (no se elimina de la DB realmente, se revierte)
    }

    //public ExpenseItem reassignExpenseItem()


}
