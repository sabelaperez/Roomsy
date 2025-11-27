package com.roomsy.backend.repository;

import com.roomsy.backend.model.ShoppingItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShoppingItemRepository extends JpaRepository<ShoppingItem, UUID> {
    Page<ShoppingItem> findByGroupId(UUID groupId, Pageable pageable);
}
