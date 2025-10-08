package com.roomsy.backend.service;

import java.util.List;
import java.util.UUID;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;

import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.User;
import com.roomsy.backend.repository.GroupRepository;

import jakarta.transaction.Transactional;

public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Group addUserToGroup(UUID groupId, UUID userId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Si quieres impedir que un usuario esté en más de un grupo simultáneamente:
        if (user.getGroup() != null) {
            if (user.getGroup().getId().equals(groupId)) {
                // ya es miembro -> nada que hacer
                return group;
            }
            throw new BadRequestException("User already belongs to another group. Remove or move before adding.");
        }

        // Mantener ambos lados de la relación
        group.addMember(user);
        userRepository.save(user);
        groupRepository.save(group);

        return group;
    }

    @Transactional
    public Group removeUserFromGroup(UUID groupId, UUID userId) {
        Group group = groupRepository.findById(groupId)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.getGroup() == null || !user.getGroup().getId().equals(groupId)) {
            throw new BadRequestException("User is not a member of the specified group.");
        }

        // Mantener ambos lados
        group.removeMember(user); // usa método auxiliar
        userRepository.save(user); // persistir el cambio (user.group = null)
        groupRepository.save(group);
        
        return group;
    }
}
