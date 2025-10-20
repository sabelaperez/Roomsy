package com.roomsy.backend.service;

import java.util.ArrayList;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.roomsy.backend.exception.InvalidOperationException;
import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.model.ExpenseItem;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.News;
import com.roomsy.backend.model.NewsType;
import com.roomsy.backend.model.SharedExpense;
import com.roomsy.backend.model.ShoppingItem;
import com.roomsy.backend.model.User;
import com.roomsy.backend.repository.GroupRepository;
import com.roomsy.backend.repository.NewsRepository;
import com.roomsy.backend.repository.UserRepository;
import com.roomsy.backend.util.InviteCodeGenerator;

import jakarta.transaction.Transactional;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final NewsRepository newsRepository;

    private static final int CODE_LENGTH = 10;
    private static final int MAX_ATTEMPTS = 6;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserRepository userRepository, NewsRepository newsRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.newsRepository = newsRepository;
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
            "User " + user.getUsername() + " added to the group", null);

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
    public Group changeGroupName(@NonNull UUID groupId, @NonNull String newName) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        group.setName(newName);
        return groupRepository.save(group);
    }

    public ArrayList<User> getGroupMembers(@NonNull UUID groupId) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        return new ArrayList<>(group.getMembers());
    }

    public ArrayList<ExpenseItem> getGroupExpenses(@NonNull UUID groupId) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        return new ArrayList<>(group.getExpenseItems());
    }

    public ArrayList<SharedExpense> getGroupSharedExpenses(@NonNull UUID groupId) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        return new ArrayList<>(group.getSharedExpenses());
    }

    public ArrayList<ShoppingItem> getGroupShoppingItems(@NonNull UUID groupId) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        return new ArrayList<>(group.getShoppingItems());
    } 
}
