package com.roomsy.backend.service;

import com.roomsy.backend.dto.ExpenseItemResponse;
import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.model.ExpenseItem;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.News;
import com.roomsy.backend.model.NewsType;
import com.roomsy.backend.model.SharedExpense;
import com.roomsy.backend.model.User;
import com.roomsy.backend.repository.ExpenseItemRepository;
import com.roomsy.backend.repository.NewsRepository;
import com.roomsy.backend.repository.SharedExpenseRepository;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExpenseService {

    private final ExpenseItemRepository expenseItemRepository;
    private final SharedExpenseRepository sharedExpenseRepository;
    private final NewsRepository newsRepository;

    @Autowired
    public ExpenseService(ExpenseItemRepository expenseItemRepository,  SharedExpenseRepository sharedExpenseRepository, NewsRepository newsRepository) {
        this.expenseItemRepository = expenseItemRepository;
        this.sharedExpenseRepository = sharedExpenseRepository;
        this.newsRepository = newsRepository;
    }

    @Transactional
    public ExpenseItem createExpenseItem(@NonNull ExpenseItem expenseItem) {
        // Xerar unha noticia do tipo EXPENSE_ADDED
        StringBuilder usersInvolvedNames = new StringBuilder();
        for (User user : expenseItem.getUsersInvolved()) {
            usersInvolvedNames.append(user.getUsername()).append(", ");
        }
        if (usersInvolvedNames.length() > 2) {
            usersInvolvedNames.setLength(usersInvolvedNames.length() - 2); // Remove last comma and space
        }   
        News addedExpenseNews = new News(expenseItem.getGroup(), expenseItem.getOwner(), NewsType.EXPENSE_ADDED,
                "Expense Added by " + expenseItem.getOwner().getUsername(), 
                expenseItem.getOwner().getUsername() + " added an expense item named '" + expenseItem.getName() + "' with amount " + 
                expenseItem.getPrice() + " € shared with " + usersInvolvedNames.toString() + ".");
        newsRepository.save(addedExpenseNews);

        return expenseItemRepository.save(expenseItem);
    }

    // Pensar nas precondicións
    @Transactional
    public void deleteExpenseItem(@NonNull UUID id, @NonNull UUID groupId) throws ResourceNotFoundException {
        ExpenseItem expenseItem = expenseItemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ExpenseItem not found"));
        if (!expenseItem.getGroup().getId().equals(groupId)) {
            throw new ResourceNotFoundException("Expense item not found in the specified group");
        }

        // Reverse that expense item in the SharedExpenses
        Double splitAmount = expenseItem.getPrice() / expenseItem.getUsersInvolved().size();

        for(User user : expenseItem.getUsersInvolved()) {
            List<User> usersInvolved = new ArrayList<>();
            usersInvolved.add(expenseItem.getOwner());
            ExpenseItem reverseExpenseItem = new ExpenseItem(
                    expenseItem.getGroup(),
                    user,
                    expenseItem.getName(),
                    expenseItem.getExpenseType(),
                    usersInvolved,
                    splitAmount,
                    expenseItem.getExpenseDate()
                    );
            generateSplitExpenses(expenseItem.getGroup(), reverseExpenseItem);
        }

        expenseItemRepository.deleteById(id);
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
            UUID notPaidId = sharedExpense.getNotPaid().getId();
            Double quantity = sharedExpense.getQuantity();

            balances.put(payerId, balances.getOrDefault(payerId, 0.0) + quantity); // ó reves?????
            balances.put(notPaidId, balances.getOrDefault(notPaidId, 0.0) - quantity);
        }

        Map<User, Double> creditorsMap = new HashMap<>();
        Map<User, Double> debtorsMap = new HashMap<>();

        Map<UUID, User> userMap = new HashMap<>();
        for (User member : group.getMembers()) { // innecesario recuperar todos los miembros del grupo
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

        // Clear existing shared expenses for the group
        for(SharedExpense se : sharedExpenses) {
            sharedExpenseRepository.deleteById(se.getId());
        }

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
        List<Map.Entry<User, Double>> debtors = new ArrayList<>(debtorsMap.entrySet());

        // Sorts in descending order
        creditors.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        debtors.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int i = 0, j = 0;

        while (i < creditors.size() && j < debtors.size()) {
            Map.Entry<User, Double> creditor = creditors.get(i);
            Map.Entry<User, Double> debtor = debtors.get(j);

            double settlementAmount = Math.min(creditor.getValue(), debtor.getValue());
            settlementAmount = roundToTwoDecimals(settlementAmount);

            if (settlementAmount >= 0.01) {
                SharedExpense settlement = new SharedExpense(group, creditor.getKey(), debtor.getKey(), settlementAmount);
                settlements.add(settlement);
            }

            creditor.setValue(creditor.getValue() - settlementAmount);
            debtor.setValue(debtor.getValue() - settlementAmount);

            if (creditor.getValue() < 0.01) i++;
            if (debtor.getValue() < 0.01) j++;
        }

        return settlements;
    }

    @Transactional
    public boolean paySharedExpense(@NonNull UUID id, @NonNull UUID groupId) {
        SharedExpense payedExpense = sharedExpenseRepository
            .findById(id).orElseThrow(() -> new ResourceNotFoundException("SharedExpense not found"));
        if(!payedExpense.getGroup().getId().equals(groupId)) {
            throw new ResourceNotFoundException("SharedExpense not found in the specified group");
        }
        boolean result = sharedExpenseRepository.deleteByIdReturningBoolean(id);
        if (result) {
            // Xerar unha noticia do tipo EXPENSE_PAID
            News paidExpNews = new News(payedExpense.getGroup(), payedExpense.getNotPaid(), NewsType.EXPENSE_PAID,
                    "An expense has been paid", "The user " + payedExpense.getNotPaid().getUsername() + " has paid an expense of amount " + payedExpense.getQuantity() + " € to " + payedExpense.getPayer().getFullName() + ".");
            newsRepository.save(paidExpNews);
            return true;
        } else {
            return false;
        }
    }

    public Page<ExpenseItemResponse> getGroupExpenses(@NonNull UUID groupId, @NonNull Pageable pageable) throws ResourceNotFoundException {
        Page<ExpenseItem> expenses = expenseItemRepository.findByGroupId(groupId, pageable);
        return expenses.map(ExpenseItemResponse::fromEntity);
    }

    public Page<SharedExpense> getGroupSharedExpenses(@NonNull UUID groupId, @NonNull Pageable pageable) throws ResourceNotFoundException {
        return sharedExpenseRepository.findByGroupId(groupId, pageable);
    }

    @Transactional
    public void deleteUser(@NonNull UUID userId) {
        List<ExpenseItem> ownedExpenses = expenseItemRepository.findByOwnerId(userId);
        List<ExpenseItem> involvedExpenses = expenseItemRepository.findByUsersInvolvedId(userId);

        for(ExpenseItem expense : ownedExpenses) {
            expenseItemRepository.deleteById(expense.getId());
        }
        for(ExpenseItem expense : involvedExpenses) {
            expenseItemRepository.deleteById(expense.getId());
        }

        List<SharedExpense> asPayer = sharedExpenseRepository.findByPayerId(userId);
        List<SharedExpense> asNotPaid = sharedExpenseRepository.findByNotPaidId(userId);

        for(SharedExpense se : asPayer) {
            sharedExpenseRepository.deleteById(se.getId());
        }
        for(SharedExpense se : asNotPaid) {
            sharedExpenseRepository.deleteById(se.getId());
        }
    }
}
