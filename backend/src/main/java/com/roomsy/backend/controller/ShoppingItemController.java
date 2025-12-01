package com.roomsy.backend.controller;

import java.util.UUID;

import com.roomsy.backend.dto.PageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;
import com.roomsy.backend.dto.ShoppingItemRequest;
import com.roomsy.backend.dto.Views;
import com.roomsy.backend.model.Category;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.ShoppingItem;
import com.roomsy.backend.service.CategoryService;
import com.roomsy.backend.service.GroupService;
import com.roomsy.backend.service.ShoppingItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@RestController
@RequestMapping("/groups/{group-id}/shopping-items")
@Tag(name = "Shopping Items", description = "Endpoints for managing shopping items within groups")
public class ShoppingItemController {
    private final ShoppingItemService shoppingItemService;
    private final GroupService groupService;
    private final CategoryService categoryService;

    @Autowired
    public ShoppingItemController(ShoppingItemService shoppingItemService, GroupService groupService, CategoryService categoryService) {
        this.shoppingItemService = shoppingItemService;
        this.groupService = groupService;
        this.categoryService = categoryService;
    }

    @Operation(summary = "Create a new shopping item", description = "Creates a new shopping item within the specified group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Shopping item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Group or category not found")
    })
    @PostMapping
    @JsonView(Views.Summary.class)
    public ResponseEntity<ShoppingItem> createShoppingItem(
            @PathVariable("group-id") UUID groupId,
            @Valid @RequestBody ShoppingItemRequest request
    ) {
        Group group = groupService.getGroupById(groupId);

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryService.getCategory(request.getCategoryId(), groupId);
        }

        ShoppingItem item = new ShoppingItem(group, category, request.getName(), request.getQuantity());
        ShoppingItem saved = shoppingItemService.createShoppingItem(item);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Delete a shopping item", description = "Deletes the specified shopping item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Shopping item deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Shopping item not found")
    })
    @DeleteMapping("/{item-id}")
    public ResponseEntity<Void> deleteShoppingItem(
        @PathVariable("item-id") UUID itemId,
        @PathVariable("group-id") UUID groupId
    ) {
        shoppingItemService.deleteShoppingItem(itemId, groupId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update shopping item category", description = "Updates the category of the specified shopping item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "404", description = "Shopping item or category not found")
    })
    @PatchMapping("/{item-id}/category")
    @JsonView(Views.Summary.class)
    public ResponseEntity<ShoppingItem> updateCategory(
            @PathVariable("item-id") UUID itemId,
            @PathVariable("group-id") UUID groupId,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        Category category = categoryService.getCategory(request.getCategoryId(), groupId);
        ShoppingItem updated = shoppingItemService.updateCategory(itemId, groupId, category);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Update shopping item name", description = "Updates the name of the specified shopping item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Name updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid name format"),
            @ApiResponse(responseCode = "404", description = "Shopping item not found")
    })
    @PatchMapping("/{item-id}/name")
    @JsonView(Views.Summary.class)
    public ResponseEntity<ShoppingItem> updateName(
            @PathVariable("item-id") UUID itemId,
            @PathVariable("group-id") UUID groupId,
            @Valid @RequestBody UpdateNameRequest request
    ) {
        ShoppingItem updated = shoppingItemService.updateName(itemId, groupId, request.getName());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Update shopping item quantity", description = "Updates the quantity of the specified shopping item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quantity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity"),
            @ApiResponse(responseCode = "404", description = "Shopping item not found")
    })
    @PatchMapping("/{item-id}/quantity")
    @JsonView(Views.Summary.class)
    public ResponseEntity<ShoppingItem> updateQuantity(
            @PathVariable("item-id") UUID itemId,
            @PathVariable("group-id") UUID groupId,
            @Valid @RequestBody UpdateQuantityRequest request
    ) {
        ShoppingItem updated = shoppingItemService.updateQuantity(itemId, groupId, request.getQuantity());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Get group shopping items", description = "Retrieves all shopping list items for the group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shopping items retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping()
    @JsonView(Views.Summary.class)
    public ResponseEntity<PageResponse<ShoppingItem>> getGroupShoppingItems(
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

        Page<ShoppingItem> shoppingItems = shoppingItemService.getGroupShoppingItems(groupId, pageable);
        return ResponseEntity.ok(new PageResponse<>(shoppingItems));
    }


    // Request DTOs
    @Schema(description = "Request object for updating a shopping item's category")
    public static class UpdateCategoryRequest {
        @NotNull
        @Schema(description = "The ID of the new category for the shopping item",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        private UUID categoryId;

        public UpdateCategoryRequest() {}

        public UUID getCategoryId() { 
            return categoryId; 
        }
        public void setCategoryId(UUID categoryId) { 
            this.categoryId = categoryId; 
        }
    }

    @Schema(description = "Request object for updating a shopping item's name")
    public static class UpdateNameRequest {
        @NotNull
        @Size(min = 3, max = 100)
        @Pattern(regexp = "^[a-zA-Z0-9 ]+$", message = "Name can only contain letters, numbers, and spaces")
        @Schema(description = "The new name for the shopping item",
                example = "Bread",
                minLength = 3,
                maxLength = 100,
                pattern = "^[a-zA-Z0-9 ]+$")
        private String name;

        public UpdateNameRequest() {}

        public String getName() { 
            return name; 
        }
        public void setName(String name) { 
            this.name = name; 
        }
    }

    @Schema(description = "Request object for updating a shopping item's quantity")
    public static class UpdateQuantityRequest {
        @NotNull
        @Min(1)
        @Schema(description = "The new quantity for the shopping item",
                example = "5",
                minimum = "1")
        private Integer quantity;

        public UpdateQuantityRequest() {}

        public Integer getQuantity() { 
            return quantity; 
        }
        public void setQuantity(Integer quantity) { 
            this.quantity = quantity; 
        }
    }
}
