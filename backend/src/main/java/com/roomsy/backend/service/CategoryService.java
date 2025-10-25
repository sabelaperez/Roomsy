package com.roomsy.backend.service;

import com.roomsy.backend.model.Category;
import com.roomsy.backend.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category createCategory(@NonNull Category category) throws IllegalArgumentException {
        if(categoryRepository.existsByGroupAndName(category.getGroup(), category.getName())) {
            throw new IllegalArgumentException("A category with that name already exists in the group");
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(@NonNull UUID id) throws IllegalArgumentException {
        if(!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category with that id does not exist");
        }
        categoryRepository.deleteById(id);
    }

    @Transactional
    public Category updateName(@NonNull UUID id, String newName) throws IllegalArgumentException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category with that id does not exist"));

        category.setName(newName);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateColor(@NonNull UUID id, String newColor) throws IllegalArgumentException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category with that id does not exist"));

        category.setColor(newColor);
        return categoryRepository.save(category);
    }
}