package com.roomsy.backend.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.roomsy.backend.dto.CategoryRequest;
import com.roomsy.backend.dto.PageResponse;
import com.roomsy.backend.dto.Views;
import com.roomsy.backend.model.Category;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.service.CategoryService;
import com.roomsy.backend.service.GroupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    @JsonView(Views.Detailed.class)
    public ResponseEntity<Category> createCategory (
            @PathVariable("group-id") UUID groupId,
            @RequestBody CategoryRequest request
    ){        
        Group group = groupService.getGroupById(groupId);
        Category category = new Category(group, request.getName(), request.getColor());
        Category savedCategory = categoryService.createCategory(category);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedCategory);
    }

    @Operation(summary = "Delete a category", description = "Deletes the specified category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
    })
    @DeleteMapping("/{category-id}")
    public ResponseEntity<Void> deleteCategory(
        @PathVariable("category-id") UUID categoryId,
        @PathVariable("group-id") UUID groupId
    ) {
        categoryService.deleteCategory(categoryId, groupId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get group categories", description = "Retrieves a paginated list of categories for the specified group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping()
    @JsonView(Views.Summary.class)
    public ResponseEntity<PageResponse<Category>> getGroupCategories(
            @PathVariable("group-id") UUID groupId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Category> categories = categoryService.getCategoriesPaginated(groupId, pageable);
        return ResponseEntity.ok(new PageResponse<>(categories));
    }

    @Operation(summary = "Get category by id", description = "Retrieves the specified category within the given group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
    })
    @GetMapping("/{category-id}")
    @JsonView(Views.Detailed.class)
    public ResponseEntity<Category> getCategoryById(
            @PathVariable("group-id") UUID groupId,
            @PathVariable("category-id") UUID categoryId
    ) {
        Category category = categoryService.getCategory(categoryId, groupId);
        return ResponseEntity.ok(category);
    }


    @Operation(summary = "Update category name", description = "Updates the name of the specified category" +
            " name must be 4-50 characters long and can only contain letters, numbers, and spaces")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category name updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid name format"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
    })
    @PatchMapping("/{category-id}/name")
    @JsonView(Views.Detailed.class)
    public ResponseEntity<Category> updateName(
        @PathVariable("category-id") UUID categoryId,
        @PathVariable("group-id") UUID groupId,
        @Valid @RequestBody UpdateNameRequest request
    ) {
        Category updatedCategory = categoryService.updateName(categoryId, groupId, request.getName());
        return ResponseEntity.ok(updatedCategory);
    }

    @Operation(summary = "Update category color", description = "Updates the color of the specified category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category color updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
    })
    @PatchMapping("/{category-id}/color")
    @JsonView(Views.Detailed.class)
    public ResponseEntity<Category> updateColor(
        @PathVariable("category-id") UUID categoryId,
        @PathVariable("group-id") UUID groupId,
        @RequestBody String newColor
    ) {
        Category updatedCategory = categoryService.updateColor(categoryId, groupId, newColor);
        return ResponseEntity.ok(updatedCategory);
    }

    // Request DTOs
    @Schema(description = "Request object for updating a category's name")
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
