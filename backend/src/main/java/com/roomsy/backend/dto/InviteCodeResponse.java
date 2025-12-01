package com.roomsy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Invite code response")
public record InviteCodeResponse(
    @Schema(description = "Invite code", example = "INVITE123") String inviteCode
) {}
