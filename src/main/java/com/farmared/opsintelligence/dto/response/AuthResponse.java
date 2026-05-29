package com.farmared.opsintelligence.dto.response;

import java.util.List;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String username,
        String email,
        List<String> roles
) {
}
