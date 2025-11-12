package com.roomsy.backend.controller;

import com.roomsy.backend.dto.CategoryRequest;
import com.roomsy.backend.dto.CategoryResponse;
import com.roomsy.backend.model.Category;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.service.CategoryService;
import com.roomsy.backend.service.GroupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/groups/{group-id}/categories")
@Tag(name = "Category", description = "Endpoints for managing categories within groups")
public class CategoryController {
    private final CategoryService categoryService;
    private final GroupService groupService;

    @Autowired
    public CategoryController(CategoryService categoryService, GroupService groupService) {
        this.categoryService = categoryService;
        this.groupService = groupService;
    }

    @Operation(summary = "Create a new category", description = "Creates a new category within the specified group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Group not found"),
    })
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

    @Operation(summary = "Delete a category", description = "Deletes the specified category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
    })
    @DeleteMapping("/{category-id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("category-id") UUID categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update category name", description = "Updates the name of the specified category" +
            " name must be 4-50 characters long and can only contain letters, numbers, and spaces")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category name updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid name format"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
    })
    @PatchMapping("/{category-id}/name")
    public ResponseEntity<CategoryResponse> updateName(@PathVariable("category-id") UUID categoryId,
            @Valid @RequestBody UpdateNameRequest request) {

        Category updatedCategory = categoryService.updateName(categoryId, request.getName());
        return ResponseEntity.ok(CategoryResponse.fromEntity(updatedCategory));
    }

    @Operation(summary = "Update category color", description = "Updates the color of the specified category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category color updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
    })
    @PatchMapping("/{category-id}/color")
    public ResponseEntity<CategoryResponse> updateColor(@PathVariable("category-id") UUID categoryId, @RequestBody String newColor) {

        Category updatedCategory = categoryService.updateColor(categoryId, newColor);
        return ResponseEntity.ok(CategoryResponse.fromEntity(updatedCategory));
    }

    // Inner class for specific update requests
    public static class UpdateNameRequest {
        @NotNull
        @Size(min = 4, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9 ]+$",
                message = "Name can only contain letters, numbers, and spaces")
        @Schema(description = "New name for the category",
                example = "Groceries")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
