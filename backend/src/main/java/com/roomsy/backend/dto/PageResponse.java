package com.roomsy.backend.dto;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import java.util.List;

@Schema(description = "Page response")
public record PageResponse<T>(
    @JsonView(Views.Basic.class) 
    @Schema(description = "Content of the page") List<T> content,
    @JsonView(Views.Basic.class) 
    @Schema(description = "Current page number") int page,
    @JsonView(Views.Basic.class) 
    @Schema(description = "Size of the page") int size,
    @JsonView(Views.Basic.class) 
    @Schema(description = "Total number of elements") long totalElements,
    @JsonView(Views.Basic.class) 
    @Schema(description = "Total number of pages") int totalPages
) {
    public PageResponse(Page<T> page) {
    this(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages()
    );
    }
}