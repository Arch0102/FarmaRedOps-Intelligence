package com.farmared.opsintelligence.dto.response;

import java.time.LocalDateTime;

public record CentroDistribucionResponse(
        Long id,
        String codigo,
        String nombre,
        String direccion,
        String ciudad,
        Boolean activo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
