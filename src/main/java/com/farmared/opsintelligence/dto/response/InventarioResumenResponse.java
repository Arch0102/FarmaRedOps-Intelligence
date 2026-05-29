package com.farmared.opsintelligence.dto.response;

import java.util.Map;

public record InventarioResumenResponse(
        Long totalInventarios,
        Long stockTotal,
        Long stockDisponible,
        Long riesgosStock,
        Long stockDisponibleTotal,
        Long stockReservadoTotal,
        Long inventariosEnQuiebre,
        Long inventariosCriticos,
        Map<String, Long> stockPorMedicamento,
        Map<String, Long> stockPorCentro,
        Map<String, Long> stockPorCategoria
) {
}
