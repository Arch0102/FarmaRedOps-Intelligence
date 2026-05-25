package com.farmared.opsintelligence.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MedicamentoProveedorResponse(
        Long id,
        BigDecimal precioReferencia,
        Integer tiempoEntregaDias,
        Boolean activo,
        Long medicamentoId,
        String medicamentoCodigo,
        String medicamentoNombre,
        Long proveedorId,
        String proveedorNit,
        String proveedorNombre,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
