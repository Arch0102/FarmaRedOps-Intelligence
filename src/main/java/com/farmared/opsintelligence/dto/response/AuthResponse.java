package com.farmared.opsintelligence.dto.response;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String username,
        String email
) {
}