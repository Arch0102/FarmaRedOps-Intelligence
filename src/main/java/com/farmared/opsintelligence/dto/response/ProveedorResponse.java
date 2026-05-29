package com.farmared.opsintelligence.dto.response;

import java.time.LocalDateTime;

public record ProveedorResponse(
        Long id,
        String nit,
        String nombre,
        String telefono,
        String correo,
        String direccion,
        Boolean activo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}