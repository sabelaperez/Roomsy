package com.roomsy.backend.util.patch;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "JSON Patch operation following RFC 6902",
        example = "{\"op\": \"replace\", \"path\": \"/name\", \"value\": \"New Name\"}")
public record JsonPatchOperation(
        @JsonAlias("op")
        @Schema(description = "The operation to perform",
                example = "replace",
                allowableValues = {"add", "remove", "replace", "copy", "move"})
        JsonPatchOperationType operation,
        @Schema(description = "JSON Pointer to the target location",
                example = "/name",
                implementation = String.class)
        JsonPointer path,
        @Schema(description = "JSON Pointer to the source location (for copy/move operations)",
                example = "/oldName",
                implementation = String.class,
                nullable = true)
        JsonPointer from,
        @Schema(description = "The value to set (for add/replace operations)",
                example = "New Value",
                nullable = true)
        JsonNode value
) { }
