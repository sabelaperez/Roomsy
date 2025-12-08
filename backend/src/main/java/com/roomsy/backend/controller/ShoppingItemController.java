package com.roomsy.backend.controller;

import java.util.List;
import java.util.UUID;

import com.roomsy.backend.dto.PageResponse;
import com.roomsy.backend.exception.ForbiddenException;
import com.roomsy.backend.model.User;
import com.roomsy.backend.security.CustomUserDetails;
import com.roomsy.backend.service.UserService;
import com.roomsy.backend.util.GroupMembershipValidator;
import com.roomsy.backend.util.patch.JsonPatchOperation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final GroupMembershipValidator groupMembershipValidator;

    @Autowired
    public ShoppingItemController(ShoppingItemService shoppingItemService, GroupService groupService, CategoryService categoryService, GroupMembershipValidator groupMembershipValidator) {
        this.shoppingItemService = shoppingItemService;
        this.groupService = groupService;
        this.categoryService = categoryService;
        this.groupMembershipValidator = groupMembershipValidator;
    }

    @Operation(summary = "Create a new shopping item", description = "Creates a new shopping item within the specified group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Shopping item created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Requester is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "Group or category not found")
    })
    @PostMapping
    @JsonView(Views.Summary.class)
    public ResponseEntity<ShoppingItem> createShoppingItem(
            @PathVariable("group-id") UUID groupId,
            @Valid @RequestBody ShoppingItemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        groupMembershipValidator.verifyGroupMembership(groupId, userDetails.getId());

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
            @ApiResponse(responseCode = "403", description = "Requester is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "Shopping item not found")
    })
    @DeleteMapping("/{item-id}")
    public ResponseEntity<Void> deleteShoppingItem(
        @PathVariable("item-id") UUID itemId,
        @PathVariable("group-id") UUID groupId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupMembershipValidator.verifyGroupMembership(groupId, userDetails.getId());

        shoppingItemService.deleteShoppingItem(itemId, groupId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update shopping item",
            description = "Updates the shopping item using JSON Patch operations. " +
                    "Supports updating 'category', 'name' and 'quantity'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shopping item updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid patch operation or invalid values"),
            @ApiResponse(responseCode = "403", description = "Requester is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "Shopping item or category not found")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "JSON Patch operations to apply to the shopping item",
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = JsonPatchOperation.class),
                    examples = {
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Replace category",
                                    summary = "Change item category by providing category object with id",
                                    value = "[{\"op\": \"replace\", \"path\": \"/category\", \"value\": { \"id\": \"062ff0be-21b1-4651-ae24-9c6274f31e9c\" }}]"
                            ),
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Replace name",
                                    summary = "Change item name",
                                    value = "[{\"op\": \"replace\", \"path\": \"/name\", \"value\": \"Whole Grain Bread\"}]"
                            ),
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Replace quantity",
                                    summary = "Change item quantity",
                                    value = "[{\"op\": \"replace\", \"path\": \"/quantity\", \"value\": 3}]"
                            ),
                            @io.swagger.v3.oas.annotations.media.ExampleObject(
                                    name = "Multiple operations",
                                    summary = "Update name and quantity",
                                    value = "[{\"op\": \"replace\", \"path\": \"/name\", \"value\": \"Eggs\"}, {\"op\": \"replace\", \"path\": \"/quantity\", \"value\": 12}]"
                            )
                    }
            )
    )
    @PatchMapping("/{item-id}")
    @JsonView(Views.Summary.class)
    public ResponseEntity<ShoppingItem> updateShoppingItem (
            @PathVariable("item-id") UUID itemId,
            @PathVariable("group-id") UUID groupId,
            @RequestBody List<JsonPatchOperation> changes,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupMembershipValidator.verifyGroupMembership(groupId, userDetails.getId());

        ShoppingItem updatedItem = shoppingItemService.updateShoppingItem(itemId, groupId, changes);
        return ResponseEntity.ok(updatedItem);
    }

    @Operation(summary = "Get group shopping items", description = "Retrieves all shopping list items for the group")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Shopping items retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Requester is not a member of the group"),
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
            @RequestParam(defaultValue = "asc") String sortDirection,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        groupMembershipValidator.verifyGroupMembership(groupId, userDetails.getId());

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ShoppingItem> shoppingItems = shoppingItemService.getGroupShoppingItems(groupId, pageable);
        return ResponseEntity.ok(new PageResponse<>(shoppingItems));
    }
}
