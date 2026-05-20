package com.farmared.opsintelligence.dto.response;

import java.math.BigDecimal;

public record DetalleOrdenResponse(
        Long id,
        Long medicamentoId,
        String medicamentoCodigo,
        String medicamentoNombre,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}