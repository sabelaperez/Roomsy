package com.roomsy.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.roomsy.backend.model.*;
import com.roomsy.backend.repository.*;
import com.roomsy.backend.util.patch.JsonPatch;
import com.roomsy.backend.util.patch.JsonPatchOperation;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.roomsy.backend.exception.InvalidOperationException;
import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.util.InviteCodeGenerator;

import jakarta.transaction.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final NewsRepository newsRepository;
    private final CategoryRepository categoryRepository;

    private static final int CODE_LENGTH = 10;
    private static final int MAX_ATTEMPTS = 6;
    private final JsonMapper jsonMapper;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserRepository userRepository, NewsRepository newsRepository, CategoryRepository categoryRepository, JsonMapper jsonMapper) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.newsRepository = newsRepository;
        this.categoryRepository = categoryRepository;
        this.jsonMapper = jsonMapper;
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

    public String getInviteCode(@NonNull UUID groupId) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        return group.getInviteCode();
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
                "User " + user.getUsername() + " added to the group", null);

        // Persistir cambios
        newsRepository.save(addedNews);
        userRepository.save(user);
        return groupRepository.save(group);
    }

    @Transactional
    public Group addUserToGroupWithInviteCode(@NonNull String inviteCode, @NonNull UUID userId) throws ResourceNotFoundException, InvalidOperationException {
        Group group = groupRepository.getGroupByInviteCode(inviteCode)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with invite code: " + inviteCode));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Si quieres impedir que un usuario esté en más de un grupo simultáneamente:
        if (user.getGroup() != null) {
            if (user.getGroup().getId().equals(group.getId())) {
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
            newsRepository.deleteByGroupId(group.getId());
            categoryRepository.deleteByGroupId(group.getId());
            groupRepository.delete(group);
            return null; // el grupo ya no existe
        } else {
            // Xerar unha noticia de tipo MEMBER_REMOVED
            News removedNews = new News(group, null, NewsType.MEMBER_REMOVED, 
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
        newsRepository.deleteByGroupId(group.getId());
        categoryRepository.deleteByGroupId(group.getId());
        groupRepository.delete(group);
    }

    @Transactional
    public Group updateGroup(@NonNull UUID groupId, @NonNull List<JsonPatchOperation> changes)
            throws IllegalArgumentException, InvalidOperationException {

        // Validate: disallow any patch that targets inviteCode
        for (JsonPatchOperation op : changes) {
            String path = String.valueOf(op.path());
            if (path == null) continue;

            // Normalize pointer and get its first segment
            String normalized = path.startsWith("/") ? path.substring(1) : path;
            String[] segments = normalized.split("/", 2);
            String first = segments.length > 0 ? segments[0] : "";

            if ("inviteCode".equalsIgnoreCase(first)) {
                throw new InvalidOperationException(
                        "Patching 'inviteCode' is not allowed. Use the /invite-code/regenerate endpoint instead."
                );
            }
        }

        Group group = getGroupById(groupId);

        JsonNode groupNode = jsonMapper.convertValue(group, JsonNode.class);

        JsonNode patchedNode = JsonPatch.apply(changes, groupNode);

        Group updatedGroup = jsonMapper.convertValue(patchedNode, Group.class);

        updatedGroup.setMembers(group.getMembers());

        return groupRepository.save(updatedGroup);
    }

    public ArrayList<User> getGroupMembers(@NonNull UUID groupId) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        return new ArrayList<>(group.getMembers());
    }

    public List<Group> getGroups() throws ResourceNotFoundException {
        return groupRepository.findAll();
    }
}
