package com.roomsy.backend.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.roomsy.backend.dto.PageResponse;
import com.roomsy.backend.dto.Views;
import com.roomsy.backend.model.News;
import com.roomsy.backend.service.NewsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/groups/{group-id}/news")
@Tag(name = "News", description = "Endpoints for managing news within groups")
public class NewsController {
    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @Operation(summary = "Get a news item by id within a group", description = "Returns a specific news item by its ID within the specified group")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "News item retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "News item not found in the specified group")
    })
    @GetMapping("/{news-id}")
    @JsonView(Views.Summary.class)
    public ResponseEntity<News> getNews(
        @PathVariable("news-id") UUID newsId,
        @PathVariable("group-id") UUID groupId
    ) {
        News news = newsService.getNews(newsId, groupId);
        return ResponseEntity.ok(news);
    }

    @Operation(summary = "Get the news of the group", description = "Retrieves all news items within the group")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "News items retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping()
    @JsonView(Views.Basic.class)
    public ResponseEntity<PageResponse<News>> getGroupNews (
        @PathVariable("group-id") UUID groupId,
        @Parameter(description = "Page number (0-indexed)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Number of items per page", example = "10")
        @RequestParam(defaultValue = "10") int size,
        @Parameter(description = "Sort field", example = "createdAt")
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @Parameter(description = "Sort direction (asc or desc)", example = "desc")
        @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc")
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<News> news = newsService.getGroupNews(groupId, pageable);
        return ResponseEntity.ok(new PageResponse<>(news));
    }
}
