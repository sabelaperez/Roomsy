package com.roomsy.backend.service;

import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.model.Category;
import com.roomsy.backend.model.ShoppingItem;
import com.roomsy.backend.repository.ShoppingItemRepository;
import jakarta.transaction.Transactional;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ShoppingItemService {

    private final ShoppingItemRepository shoppingItemRepository;

    @Autowired
    public ShoppingItemService(ShoppingItemRepository shoppingItemRepository) {
        this.shoppingItemRepository = shoppingItemRepository;
    }

    public ShoppingItem getShoppingItem(@NonNull UUID id, @NonNull UUID groupId) {
        ShoppingItem shoppingItem = shoppingItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShoppingItem not found with id: " + id));
        if(!shoppingItem.getGroup().getId().equals(groupId)){
            throw new IllegalArgumentException("ShoppingItem not found in the specified group");
        }
        return shoppingItem;
    }

    public boolean existShoppingItem(@NonNull UUID id, @NonNull UUID groupId) {
        ShoppingItem shoppingItem = shoppingItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShoppingItem not found with id: " + id));
        if(!shoppingItem.getGroup().getId().equals(groupId)){
            throw new IllegalArgumentException("ShoppingItem not found in the specified group");
        }
        return true;
    }

    @Transactional
    public ShoppingItem createShoppingItem(@NonNull ShoppingItem shoppingItem) {
        return shoppingItemRepository.save(shoppingItem);
    }

     @Transactional
    public void deleteShoppingItem(@NonNull UUID id, @NonNull UUID groupId) throws IllegalArgumentException {
        if(existShoppingItem(id, groupId)) {
            shoppingItemRepository.deleteById(id);
        }
    }

    public ShoppingItem updateCategory(@NonNull UUID id, @NonNull UUID groupId, @NonNull Category category) throws ResourceNotFoundException {
        ShoppingItem shoppingItem = getShoppingItem(id, groupId);
        shoppingItem.setCategory(category);
        return shoppingItemRepository.save(shoppingItem);
    }

    public ShoppingItem updateName(@NonNull UUID id, @NonNull UUID groupId, @NonNull String name) throws ResourceNotFoundException {
        ShoppingItem shoppingItem = getShoppingItem(id, groupId);
        shoppingItem.setName(name);
        return shoppingItemRepository.save(shoppingItem);
    }

    public ShoppingItem updateQuantity(@NonNull UUID id, @NonNull UUID groupId, @NonNull Integer quantity) throws ResourceNotFoundException {
        ShoppingItem shoppingItem = getShoppingItem(id, groupId);
        shoppingItem.setQuantity(quantity);
        return shoppingItemRepository.save(shoppingItem);
    }

    public Page<ShoppingItem> getGroupShoppingItems(@NonNull UUID groupId, @NonNull Pageable pageable) throws ResourceNotFoundException {
        return shoppingItemRepository.findByGroupId(groupId, pageable);
    }
}
