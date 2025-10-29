package com.roomsy.backend.controller;

import com.roomsy.backend.dto.CategoryRequest;
import com.roomsy.backend.dto.CategoryResponse;
import com.roomsy.backend.exception.ResourceNotFoundException;
import com.roomsy.backend.model.Category;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.service.CategoryService;
import com.roomsy.backend.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/groups/{groupId}/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final GroupService groupService;

    @Autowired
    public CategoryController(CategoryService categoryService, GroupService groupService) {
        this.categoryService = categoryService;
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory (
            @PathVariable UUID groupId,
            @RequestBody CategoryRequest request)  {
        Group group = groupService.getGroupById(groupId);

        Category category = new Category(group, request.getName(), request.getColor());
        Category savedCategory = categoryService.createCategory(category);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CategoryResponse.fromEntity(savedCategory));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{categoryId}/name")
    public ResponseEntity<CategoryResponse> updateName(@PathVariable UUID categoryId,
            @Valid @RequestBody UpdateNameRequest request) {

        Category updatedCategory = categoryService.updateName(categoryId, request.getName());
        return ResponseEntity.ok(CategoryResponse.fromEntity(updatedCategory));
    }

    @PatchMapping("/{categoryId}/color")
    public ResponseEntity<CategoryResponse> updateColor(@PathVariable UUID categoryId, @RequestBody String newColor) {

        Category updatedCategory = categoryService.updateColor(categoryId, newColor);
        return ResponseEntity.ok(CategoryResponse.fromEntity(updatedCategory));
    }

    // Inner class for specific update requests
    public static class UpdateNameRequest {
        @jakarta.validation.constraints.NotNull
        @jakarta.validation.constraints.Size(min = 4, max = 50)
        @jakarta.validation.constraints.Pattern(regexp = "^[a-zA-Z0-9 ]+$",
                message = "Name can only contain letters, numbers, and spaces")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
