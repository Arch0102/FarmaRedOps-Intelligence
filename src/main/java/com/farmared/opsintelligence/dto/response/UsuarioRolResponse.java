package com.farmared.opsintelligence.dto.response;

import java.time.LocalDateTime;

public record UsuarioRolResponse(
        Long id,
        Long usuarioId,
        String username,
        Long rolId,
        String rolNombre,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
