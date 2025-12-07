package com.roomsy.backend.service;

import com.roomsy.backend.exception.InvalidOperationException;
import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.model.Category;
import com.roomsy.backend.model.CleaningTask;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.ShoppingItem;
import com.roomsy.backend.repository.ShoppingItemRepository;
import com.roomsy.backend.util.patch.JsonPatch;
import com.roomsy.backend.util.patch.JsonPatchOperation;
import jakarta.transaction.Transactional;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

@Service
public class ShoppingItemService {

    private final ShoppingItemRepository shoppingItemRepository;
    private final JsonMapper jsonMapper;

    @Autowired
    public ShoppingItemService(ShoppingItemRepository shoppingItemRepository, JsonMapper jsonMapper) {
        this.shoppingItemRepository = shoppingItemRepository;
        this.jsonMapper = jsonMapper;
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

    @Transactional
    public ShoppingItem updateShoppingItem(@NonNull UUID shoppingItemId, @NonNull UUID groupId, @NonNull List<JsonPatchOperation> changes)
            throws IllegalArgumentException, InvalidOperationException {

        ShoppingItem shoppingItem = getShoppingItem(shoppingItemId, groupId);

        JsonNode shoppingItemNode = jsonMapper.convertValue(shoppingItem, JsonNode.class);

        JsonNode patchedNode = JsonPatch.apply(changes, shoppingItemNode);

        ShoppingItem updatedShoppingItem = jsonMapper.convertValue(patchedNode, ShoppingItem.class);

        updatedShoppingItem.setGroup(shoppingItem.getGroup());

        return shoppingItemRepository.save(updatedShoppingItem);

    }

    public Page<ShoppingItem> getGroupShoppingItems(@NonNull UUID groupId, @NonNull Pageable pageable) throws ResourceNotFoundException {
        return shoppingItemRepository.findByGroupId(groupId, pageable);
    }
}
