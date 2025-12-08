package com.roomsy.backend.service;

import com.roomsy.backend.exception.InvalidOperationException;
import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.model.ShoppingItem;
import com.roomsy.backend.repository.ShoppingItemRepository;
import com.roomsy.backend.util.patch.JsonPatch;
import com.roomsy.backend.util.patch.JsonPatchOperation;
import com.roomsy.backend.util.patch.JsonPatchOperationType;
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

        // Validate: quantity must be >= 1 for operations that set it
        for (JsonPatchOperation op : changes) {
            if (op == null) continue;
            if (op.path() == null) continue;

            // Normalize pointer and get its first segment
            String path = op.path().toString(); // e.g. "/quantity"
            String normalized = path.startsWith("/") ? path.substring(1) : path;
            String[] segments = normalized.split("/", 2);
            String first = segments.length > 0 ? segments[0] : "";

            // Only validate when patch targets the "quantity" field and operation sets a value
            if ("quantity".equalsIgnoreCase(first)) {
                // Only apply this validation for operations that provide a value
                JsonPatchOperationType type = op.operation();
                if (type == JsonPatchOperationType.ADD || type == JsonPatchOperationType.REPLACE) {
                    JsonNode val = op.value();
                    if (val == null) {
                        throw new InvalidOperationException("Patching 'quantity' requires a value.");
                    }

                    Integer numeric = null;

                    // If it's a number node
                    if (val.isNumber()) {
                        numeric = val.asInt();
                    } else if (val.isString()) {
                        // try to parse numeric string
                        try {
                            numeric = Integer.parseInt(val.asString().trim());
                        } catch (NumberFormatException ignored) {
                            // will throw below as invalid
                        }
                    }

                    if (numeric == null) {
                        throw new InvalidOperationException("Patching 'quantity' must provide a numeric value.");
                    }

                    if (numeric < 1) {
                        throw new InvalidOperationException(
                                "Patching 'quantity' is not allowed with values less than 1."
                        );
                    }
                }
            }
        }


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
