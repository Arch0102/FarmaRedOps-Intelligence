package com.farmared.opsintelligence.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioResponse(
        Long id,
        String username,
        String email,
        String nombreCompleto,
        Boolean activo,
        List<String> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
