package com.roomsy.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.roomsy.backend.dto.ExpenseItemResponse;
import com.roomsy.backend.model.*;
import com.roomsy.backend.repository.*;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.roomsy.backend.exception.InvalidOperationException;
import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.util.InviteCodeGenerator;

import jakarta.transaction.Transactional;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final NewsRepository newsRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseItemRepository expenseItemRepository;

    private static final int CODE_LENGTH = 10;
    private static final int MAX_ATTEMPTS = 6;
    private final SharedExpenseRepository sharedExpenseRepository;
    private final ShoppingItemRepository shoppingItemRepository;
    private final CleaningTaskRepository cleaningTaskRepository;


    @Autowired
    public GroupService(GroupRepository groupRepository, UserRepository userRepository, NewsRepository newsRepository, CategoryRepository categoryRepository, ExpenseItemRepository expenseItemRepository, SharedExpenseRepository sharedExpenseRepository, ShoppingItemRepository shoppingItemRepository, CleaningTaskRepository cleaningTaskRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.newsRepository = newsRepository;
        this.categoryRepository = categoryRepository;
        this.expenseItemRepository = expenseItemRepository;
        this.sharedExpenseRepository = sharedExpenseRepository;
        this.shoppingItemRepository = shoppingItemRepository;
        this.cleaningTaskRepository = cleaningTaskRepository;
    }

    public Group getGroupById(@NonNull UUID groupId) throws ResourceNotFoundException {
        return groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
    }

    private String generateUniqueCode() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            String code = InviteCodeGenerator.generate(CODE_LENGTH);
            if (!groupRepository.existsByInviteCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Unable to generate unique invite code");
    }

    @Transactional
    public String regenerateInviteCode(@NonNull UUID groupId) throws ResourceNotFoundException {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String newCode = InviteCodeGenerator.generate(CODE_LENGTH);
            if (groupRepository.existsByInviteCode(newCode)) {
                continue; // colisión, reintentar
            }
            try {
                Group group = getGroupById(groupId);
                group.setInviteCode(newCode);
                groupRepository.saveAndFlush(group); // forzar persistencia inmediata
                return newCode;
            } catch (DataIntegrityViolationException e) {
                // posible condición de carrera: otro hilo creó el mismo code entre exists y save
                // reintentar
            }
        }
        throw new IllegalStateException("Could not generate unique invite code after retries");
    }

    @Transactional
    public Group createGroup(@NonNull Group group, @NonNull User creator) {
        group.setInviteCode(generateUniqueCode());
        group.addMember(creator);
        Group savedGroup = groupRepository.save(group);
        creator.setGroup(savedGroup); // mantener ambos lados
        userRepository.save(creator);
        return savedGroup;
    }

    @Transactional
    public Group addUserToGroup(@NonNull UUID groupId, @NonNull UUID userId) throws ResourceNotFoundException, InvalidOperationException {
        Group group = getGroupById(groupId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Si quieres impedir que un usuario esté en más de un grupo simultáneamente:
        if (user.getGroup() != null) {
            if (user.getGroup().getId().equals(groupId)) {
                // ya es miembro -> nada que hacer
                return group;
            }
            throw new InvalidOperationException("User already belongs to another group. Remove or move before adding.");
        }

        // Mantener ambos lados de la relación
        group.addMember(user);
        
        // Xerar unha noticia de tipo MEMBER_ADDED
        News addedNews = new News(group, user, NewsType.MEMBER_ADDED, 
            "user", "pruebaaaa"); // todo: hardcodeado

        // Persistir cambios
        newsRepository.save(addedNews);
        userRepository.save(user);
        return groupRepository.save(group);
    }

    @Transactional
    public Group removeUserFromGroup(@NonNull UUID groupId, @NonNull UUID userId) throws ResourceNotFoundException, InvalidOperationException {
        Group group = getGroupById(groupId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getGroup() == null || !user.getGroup().getId().equals(groupId)) {
            throw new InvalidOperationException("User is not a member of the specified group.");
        }

        // Mantener ambos lados
        group.removeMember(user); // usa método auxiliar
        user.setGroup(null); // desvincular usuario
        userRepository.save(user); // persistir el cambio (user.group = null)

        // Si no quedan miembros, eliminar el grupo entero
        if (group.getMembers() == null || group.getMembers().isEmpty()) {
            groupRepository.delete(group);
            return null; // el grupo ya no existe
        } else {
            // Xerar unha noticia de tipo MEMBER_REMOVED
            News removedNews = new News(group, user, NewsType.MEMBER_REMOVED, 
                "User " + user.getUsername() + " removed from the group", null);

            // Persistir cambios
            newsRepository.save(removedNews);
            return groupRepository.save(group);
        }
    }

    @Transactional
    public void deleteGroup(@NonNull UUID groupId) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        List<User> usersCopy = new ArrayList<>(group.getMembers());

        for (User user : usersCopy) {
            user.setGroup(null);
            userRepository.save(user);
        }

        group.getMembers().clear();
        groupRepository.delete(group);
    }

    @Transactional
    public Group changeGroupName(@NonNull UUID groupId, @NonNull String newName) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        group.setName(newName);
        return groupRepository.save(group);
    }

    public ArrayList<User> getGroupMembers(@NonNull UUID groupId) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        return new ArrayList<>(group.getMembers());
    }

    // todo: change those methods to his respectives services

    public Page<ExpenseItemResponse> getGroupExpenses(@NonNull UUID groupId, @NonNull Pageable pageable) throws ResourceNotFoundException {
        Page<ExpenseItem> expenses = expenseItemRepository.findByGroupId(groupId, pageable);
        return expenses.map(ExpenseItemResponse::fromEntity);
    }

    public Page<SharedExpense> getGroupSharedExpenses(@NonNull UUID groupId, @NonNull Pageable pageable) throws ResourceNotFoundException {
        return sharedExpenseRepository.findByGroupId(groupId, pageable);
    }

    public Page<ShoppingItem> getGroupShoppingItems(@NonNull UUID groupId, @NonNull Pageable pageable) throws ResourceNotFoundException {
        return shoppingItemRepository.findByGroupId(groupId, pageable);
    }


    public Page<CleaningTask> getGroupCleaningTasks(@NonNull UUID groupId, @NonNull Pageable pageable) throws ResourceNotFoundException {
        return cleaningTaskRepository.findByGroupId(groupId, pageable);
    }

    public List<Group> getGroups() throws ResourceNotFoundException {
        return groupRepository.findAll();
    }
}
