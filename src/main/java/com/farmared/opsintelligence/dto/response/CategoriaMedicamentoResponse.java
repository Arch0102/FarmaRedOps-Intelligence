package com.farmared.opsintelligence.dto.response;

import java.time.LocalDateTime;

public record CategoriaMedicamentoResponse(
        Long id,
        String nombre,
        String descripcion,
        Boolean activo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
