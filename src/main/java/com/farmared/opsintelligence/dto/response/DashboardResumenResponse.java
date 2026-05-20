package com.farmared.opsintelligence.dto.response;

import java.math.BigDecimal;

public record DashboardResumenResponse(
        Long totalInventarios,
        Long totalStockCritico,
        Long totalLotesProximosVencer,
        Long totalOrdenesPendientes,
        Long totalAlertasPendientes,
        BigDecimal valorTotalOrdenesPendientes
) {
}