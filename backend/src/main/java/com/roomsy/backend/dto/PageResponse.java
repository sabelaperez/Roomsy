package com.roomsy.backend.dto;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        @JsonView(Views.Summary.class)
        List<T> content,

        @JsonView(Views.Summary.class)
        int page,

        @JsonView(Views.Summary.class)
        int size,

        @JsonView(Views.Summary.class)
        long totalElements,

        @JsonView(Views.Summary.class)
        int totalPages
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