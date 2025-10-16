package com.roomsy.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.roomsy.backend.model.Group;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    Optional<Group> findById(UUID id);
    
    Optional<Group> getGroupByInviteCode(String inviteCode);
}
