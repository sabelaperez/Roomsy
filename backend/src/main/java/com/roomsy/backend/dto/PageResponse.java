package com.roomsy.backend.dto;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
    @JsonView(Views.Basic.class) List<T> content,
    @JsonView(Views.Basic.class) int page,
    @JsonView(Views.Basic.class) int size,
    @JsonView(Views.Basic.class) long totalElements,
    @JsonView(Views.Basic.class) int totalPages
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