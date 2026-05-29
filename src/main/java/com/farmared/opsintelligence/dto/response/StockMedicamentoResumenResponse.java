package com.farmared.opsintelligence.dto.response;

public record StockMedicamentoResumenResponse(
        Long inventarioId,
        Long medicamentoId,
        String medicamentoCodigo,
        String medicamentoNombre,
        String categoriaNombre,
        Long centroDistribucionId,
        String centroDistribucionNombre,
        Integer stockActual,
        Integer stockMinimo,
        Integer puntoReorden
) {
}
