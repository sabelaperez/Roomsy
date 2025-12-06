package com.roomsy.backend.repository;

import com.roomsy.backend.model.Category;
import com.roomsy.backend.model.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsByGroupAndName(Group group, String name);
    Page<Category> findByGroupId(UUID groupId, Pageable pageable);
    void deleteByGroupId(UUID groupId);
}
