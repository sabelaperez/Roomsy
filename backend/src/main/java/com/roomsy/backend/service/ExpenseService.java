package com.roomsy.backend.service;

import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.model.ExpenseItem;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.SharedExpense;
import com.roomsy.backend.model.User;
import com.roomsy.backend.repository.ExpenseItemRepository;
import com.roomsy.backend.repository.SharedExpenseRepository;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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

    /**
     * Calculates and generates settlement transactions (SharedExpense entities)
     * Returns the minimum number of transactions needed to settle all debts
     */
    @Transactional
    public List<SharedExpense> generateSplitExpenses(Group group, ExpenseItem expenseItem) {
        List<SharedExpense> sharedExpenses = sharedExpenseRepository.findByGroup(group);

        Map<UUID, Double> balances = calculateNetBalances(expenseItem);

        // Add existing shared expenses to the balance
        for (SharedExpense sharedExpense : sharedExpenses) {
            UUID payerId = sharedExpense.getPayer().getId();
            UUID payToId = sharedExpense.getPayTo().getId();
            Double quantity = sharedExpense.getQuantity();

            balances.put(payerId, balances.getOrDefault(payerId, 0.0) - quantity);
            balances.put(payToId, balances.getOrDefault(payerId, 0.0) + quantity);
        }

        Map<User, Double> creditorsMap = new HashMap<>();
        Map<User, Double> debtorsMap = new HashMap<>();

        Map<UUID, User> userMap = new HashMap<>();
        for (User member : group.getMembers()) {
            userMap.put(member.getId(), member);
        }

        // Add users and balance to creditorsMap and debtorsMap
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            double balance = roundToTwoDecimals(entry.getValue());
            if (Math.abs(balance) < 0.01) continue;

            User user = userMap.get(entry.getKey());
            if (user == null) continue;

            if (balance > 0) {
                creditorsMap.put(user, balance);
            } else if (balance < 0) {
                debtorsMap.put(user, -balance);
            }
        }

        List<SharedExpense> settlements = minimizeTransactions(group, creditorsMap, debtorsMap);

        return sharedExpenseRepository.saveAll(settlements);
    }

    /**
     * Calculates net balance for each user
     */
    private Map<UUID, Double> calculateNetBalances(ExpenseItem expense) {
        Map<UUID, Double> balances = new HashMap<>();

        UUID ownerId = expense.getOwner().getId();
        double totalPrice = expense.getPrice();
        List<User> involved = expense.getUsersInvolved();

        // Owner paid the full amount
        balances.put(ownerId, balances.getOrDefault(ownerId, 0.0) + totalPrice);

        double sharePerPerson = totalPrice / involved.size();

        for (User user : involved) {
            UUID userId = user.getId();
            balances.put(userId, balances.getOrDefault(userId, 0.0) - sharePerPerson);
        }

        return balances;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Creates settlement transactions using greedy algorithm
     */
    private List<SharedExpense> minimizeTransactions(Group group, Map<User, Double> creditorsMap, Map<User, Double> debtorsMap) {
        List<SharedExpense> settlements = new ArrayList<>();

        List<Map.Entry<User, Double>> creditors = new ArrayList<>(creditorsMap.entrySet());
        List<Map.Entry<User, Double>> debtors = new ArrayList<>(creditorsMap.entrySet());

        // Sorts in descending order
        creditors.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        creditors.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int i = 0, j = 0;

        while (i < creditors.size() && j < debtors.size()) {
            Map.Entry<User, Double> creditor = creditors.get(i);
            Map.Entry<User, Double> debtor = debtors.get(j);

            double settlementAmount = Math.min(creditor.getValue(), debtor.getValue());
            settlementAmount = roundToTwoDecimals(settlementAmount);

            if (settlementAmount >= 0.01) {
                SharedExpense settlement = new SharedExpense(group, debtor.getKey(), creditor.getKey(), settlementAmount);
                settlements.add(settlement);
            }

            creditor.setValue(creditor.getValue() - settlementAmount);
            debtor.setValue(debtor.getValue() - settlementAmount);

            if (creditor.getValue() < 0.01) i++;
            if (debtor.getValue() < 0.01) j++;
        }

        return settlements;
    }


}
