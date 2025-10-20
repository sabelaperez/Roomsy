package com.roomsy.backend.service;

import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.model.ExpenseItem;
import com.roomsy.backend.model.SharedExpense;
import com.roomsy.backend.repository.ExpenseItemRepository;
import com.roomsy.backend.repository.SharedExpenseRepository;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {

    private ExpenseItemRepository expenseItemRepository;
    private SharedExpenseRepository sharedExpenseRepository;

    @Autowired
    public ExpenseService(ExpenseItemRepository expenseItemRepository,  SharedExpenseRepository sharedExpenseRepository) {
        this.expenseItemRepository = expenseItemRepository;
        this.sharedExpenseRepository = sharedExpenseRepository;
    }

    @Transactional
    public ExpenseItem createExpenseItem(@NonNull ExpenseItem expenseItem) throws Exception {
        return expenseItemRepository.save(expenseItem);
    }

    @Transactional
    public void deleteExpenseItem(@NonNull UUID id) throws ResourceNotFoundException {
        if(!expenseItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("ExpenseItem with that id does not exist");
        }

        expenseItemRepository.deleteById(id);
        // TODO: gasto compensatorio (no se elimina de la DB realmente, se revierte)
    }


    //public List<SharedExpense> generateSplitExpenses()


}
