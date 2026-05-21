package com.farmared.opsintelligence.dto.response;

import java.time.LocalDateTime;

public record MedicamentoResponse(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        String principioActivo,
        String concentracion,
        String presentacion,
        String unidadMedida,
        Integer stockMinimo,
        Integer stockMaximo,
        Integer puntoReorden,
        Boolean activo,
        Long categoriaMedicamentoId,
        String categoriaMedicamentoNombre,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}