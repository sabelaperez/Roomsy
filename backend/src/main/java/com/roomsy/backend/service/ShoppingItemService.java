package com.roomsy.backend.service;

import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.model.Category;
import com.roomsy.backend.model.ShoppingItem;
import com.roomsy.backend.repository.ShoppingItemRepository;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ShoppingItemService {

    private ShoppingItemRepository shoppingItemRepository;

    @Autowired
    public ShoppingItemService(ShoppingItemRepository shoppingItemRepository) {
        this.shoppingItemRepository = shoppingItemRepository;
    }

    @Transactional
    public ShoppingItem createShoppingItem(@NonNull ShoppingItem shoppingItem) throws Exception {
        return shoppingItemRepository.save(shoppingItem);
    }

     @Transactional
    public void deleteShoppingItem(@NonNull UUID id) throws Exception {
        if(!shoppingItemRepository.existsById(id)) {
            throw new IllegalArgumentException("ShoppingItem with that id does not exist");
        }
        shoppingItemRepository.deleteById(id);
    }

    public ShoppingItem updateCategory(@NonNull UUID id, @NonNull Category category) throws ResourceNotFoundException {
        ShoppingItem shoppingItem = shoppingItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShoppingItem with that id does not exist"));

        shoppingItem.setCategory(category);
        return shoppingItemRepository.save(shoppingItem);
    }

    public ShoppingItem updateName(@NonNull UUID id, @NonNull String name) throws ResourceNotFoundException {
        ShoppingItem shoppingItem = shoppingItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShoppingItem with that id does not exist"));

        shoppingItem.setName(name);
        return shoppingItemRepository.save(shoppingItem);
    }

    public ShoppingItem updateQuantity(@NonNull UUID id, @NonNull Integer quantity) throws ResourceNotFoundException {
        ShoppingItem shoppingItem = shoppingItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ShoppingItem with that id does not exist"));

        shoppingItem.setQuantity(quantity);
        return shoppingItemRepository.save(shoppingItem);
    }

}
