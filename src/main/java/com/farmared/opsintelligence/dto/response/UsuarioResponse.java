package com.farmared.opsintelligence.dto.response;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String username,
        String email,
        String nombreCompleto,
        Boolean activo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
