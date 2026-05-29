package com.farmared.opsintelligence.dto.response;

public record MedicamentoRotacionResponse(
        Long medicamentoId,
        String medicamentoCodigo,
        String medicamentoNombre,
        Long totalSalidas
) {
}
