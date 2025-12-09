package com.roomsy.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.roomsy.backend.dto.GroupResponse;
import com.roomsy.backend.model.*;
import com.roomsy.backend.repository.*;
import com.roomsy.backend.util.patch.JsonPatch;
import com.roomsy.backend.util.patch.JsonPatchOperation;
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
                continue; // try again
            }
            try {
                Group group = getGroupById(groupId);
                group.setInviteCode(newCode);
                groupRepository.saveAndFlush(group); 
                return newCode;
            } catch (DataIntegrityViolationException e) {
                // collision, try again
            }
        }
        throw new IllegalStateException("Could not generate unique invite code after retries");
    }

    @Transactional
    public Group createGroup(@NonNull Group group, @NonNull User creator) {
        group.setInviteCode(generateUniqueCode());
        group.addMember(creator);
        Group savedGroup = groupRepository.save(group);
        creator.setGroup(savedGroup); 
        userRepository.save(creator);
        return savedGroup;
    }

    @Transactional
    public Group addUserToGroup(@NonNull UUID groupId, @NonNull UUID userId) throws ResourceNotFoundException, InvalidOperationException {
        Group group = getGroupById(groupId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // one group per user restriction
        if (user.getGroup() != null) {
            if (user.getGroup().getId().equals(groupId)) {
                // already a member
                return group;
            }
            throw new InvalidOperationException("User already belongs to another group. Remove or move before adding.");
        }

        group.addMember(user);
        
        News addedNews = new News(group, user, NewsType.MEMBER_ADDED, 
                "User " + user.getUsername() + " added to the group", null);

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

        // one group per user restriction
        if (user.getGroup() != null) {
            if (user.getGroup().getId().equals(group.getId())) {
                // already a member
                return group;
            }
            throw new InvalidOperationException("User already belongs to another group. Remove or move before adding.");
        }

        group.addMember(user);

        News addedNews = new News(group, user, NewsType.MEMBER_ADDED, 
                "User " + user.getUsername() + " added to the group", null);

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

        group.removeMember(user); 
        user.setGroup(null); 
        userRepository.save(user); 

        // last member leaves, delete group
        if (group.getMembers() == null || group.getMembers().isEmpty()) {
            newsRepository.deleteByGroupId(group.getId());
            categoryRepository.deleteByGroupId(group.getId());
            groupRepository.delete(group);
            return null; 
        } else {
            News removedNews = new News(group, null, NewsType.MEMBER_REMOVED, 
                "User " + user.getUsername() + " removed from the group", null); 

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
        updatedGroup.setCleaningTasks(group.getCleaningTasks());
        updatedGroup.setExpenseItems(group.getExpenseItems());
        updatedGroup.setSharedExpenses(group.getSharedExpenses());
        updatedGroup.setShoppingItems(group.getShoppingItems());

        return groupRepository.save(updatedGroup);
    }

    public ArrayList<User> getGroupMembers(@NonNull UUID groupId) throws ResourceNotFoundException {
        Group group = getGroupById(groupId);
        return new ArrayList<>(group.getMembers());
    }

    public Page<GroupResponse> getGroups(@NonNull Pageable pageable) throws ResourceNotFoundException {
        Page<Group> groups = groupRepository.findAll(pageable);
        return groups.map(GroupResponse::fromEntity);
    }
}
