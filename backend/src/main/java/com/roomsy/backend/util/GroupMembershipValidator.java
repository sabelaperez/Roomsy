package com.roomsy.backend.util;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.roomsy.backend.exception.ForbiddenException;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.User;
import com.roomsy.backend.service.GroupService;
import com.roomsy.backend.service.UserService;

@Component
public class GroupMembershipValidator {

    private final UserService userService;
    private final GroupService groupService;

    public GroupMembershipValidator(UserService userService, GroupService groupService) {
        this.userService = userService;
        this.groupService = groupService;
    }

    public void verifyGroupMembership(UUID groupId, UUID userId) {
        User user = userService.getUserById(userId);

        // Admin users have access to all groups
        if (user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().name())) {
            return;
        }

        Group group = groupService.getGroupById(groupId);
        if (!group.getMembers().contains(user)) {
            throw new ForbiddenException("Requester is not a member of this group");
        }
    }
}