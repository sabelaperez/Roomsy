package com.roomsy.backend.service;

import java.util.ArrayList;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.roomsy.backend.exception.InvalidOperationException;
import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.model.ExpenseItem;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.SharedExpense;
import com.roomsy.backend.model.ShoppingItem;
import com.roomsy.backend.model.User;
import com.roomsy.backend.repository.GroupRepository;
import com.roomsy.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public Group getGroupById(UUID groupId) throws ResourceNotFoundException {
        return groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
    }

    @Transactional
    public Group createGroup(String name, String inviteCode, User creator) {
        Group group = new Group(name, inviteCode, creator);
        return groupRepository.save(group);
    }

    @Transactional
    public Group addUserToGroup(UUID groupId, UUID userId) throws ResourceNotFoundException, InvalidOperationException {
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
        userRepository.save(user);
        
        return groupRepository.save(group);
    }

    @Transactional
    public Group removeUserFromGroup(UUID groupId, UUID userId) throws ResourceNotFoundException, InvalidOperationException {
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
            return groupRepository.save(group);
        }
    }

    @Transactional
    public Group changeGroupName(UUID groupId, String newName) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        group.setName(newName);
        return groupRepository.save(group);
    }

    public ArrayList<User> getGroupMembers(UUID groupId) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        return new ArrayList<>(group.getMembers());
    }

    public ArrayList<ExpenseItem> getGroupExpenses(UUID groupId) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        return new ArrayList<>(group.getExpenseItems());
    }

    public ArrayList<SharedExpense> getGroupSharedExpenses(UUID groupId) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        return new ArrayList<>(group.getSharedExpenses());
    }

    public ArrayList<ShoppingItem> getGroupShoppingItems(UUID groupId) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        return new ArrayList<>(group.getShoppingItems());
    } 
    
}
