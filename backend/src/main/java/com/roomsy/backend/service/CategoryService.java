package com.roomsy.backend.service;

import com.roomsy.backend.model.Category;
import com.roomsy.backend.repository.CategoryRepository;

import com.roomsy.backend.util.patch.JsonPatch;
import com.roomsy.backend.util.patch.JsonPatchOperation;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;

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
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final JsonMapper jsonMapper;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository,  JsonMapper jsonMapper) {
        this.categoryRepository = categoryRepository;
        this.jsonMapper = jsonMapper;
    }

    public Category getCategory(@NonNull UUID id, @NotNull UUID groupId) throws IllegalArgumentException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        if (!category.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Category not found in the specified group");
        }
        return category;
    }

    public Page<Category> getCategoriesPaginated(@NonNull UUID groupId, Pageable pageable) {
        return categoryRepository.findByGroupId(groupId, pageable);
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
    public Category updateCategory(@NonNull UUID id, @NonNull UUID groupId, List<JsonPatchOperation> changes)
            throws IllegalArgumentException {
        Category category = getCategory(id, groupId);

        JsonNode categoryNode = jsonMapper.convertValue(category, JsonNode.class);

        JsonNode patchedNode = JsonPatch.apply(changes, categoryNode);

        Category updatedCategory = jsonMapper.convertValue(patchedNode, Category.class);

        updatedCategory.setGroup(category.getGroup());

        return categoryRepository.save(updatedCategory);
    }
}