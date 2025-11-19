package com.roomsy.backend.service;

import com.roomsy.backend.model.Category;
import com.roomsy.backend.repository.CategoryRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category getCategory(@NonNull UUID id, @NotNull UUID groupId) throws IllegalArgumentException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        if (!category.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Category not found in the specified group");
        }
        return category;
    }

    public boolean existCategory(@NonNull UUID id, @NotNull UUID groupId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        if (!category.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Category not found in the specified group");
        }
        return true;
    }

    @Transactional
    public Category createCategory(@NonNull Category category) throws IllegalArgumentException {
        if(categoryRepository.existsByGroupAndName(category.getGroup(), category.getName())) {
            throw new IllegalArgumentException("A category with that name already exists in the group");
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(@NonNull UUID id, @NonNull UUID groupId) throws IllegalArgumentException {
        if(existCategory(id, groupId)) {
            categoryRepository.deleteById(id);
        }
    }

    @Transactional
    public Category updateName(@NonNull UUID id, @NonNull UUID groupId, String newName) throws IllegalArgumentException {
        Category category = getCategory(id, groupId);
        category.setName(newName);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateColor(@NonNull UUID id, @NonNull UUID groupId, String newColor) throws IllegalArgumentException {
        Category category = getCategory(id, groupId);
        category.setColor(newColor);
        return categoryRepository.save(category);
    }
}