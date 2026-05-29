package com.farmared.opsintelligence.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardResumenResponse(
        Long totalMedicamentosActivos,
        Long totalProveedoresActivos,
        Long totalInventarios,
        Long totalStockCritico,
        Long totalLotesProximosVencer,
        Long totalOrdenesPendientes,
        Long totalAlertasPendientes,
        BigDecimal valorTotalOrdenesPendientes,
        Map<String, Long> ordenesPorEstado,
        List<MovimientoInventarioResponse> movimientosRecientes,
        List<StockMedicamentoResumenResponse> medicamentosProximosAgotarse,
        List<StockMedicamentoResumenResponse> medicamentosEnQuiebre,
        List<MedicamentoRotacionResponse> medicamentosMayorRotacion,
        Map<String, Long> stockPorCategoria,
        Map<String, Long> stockPorCentro,
        List<AlertaStockResponse> alertasStock
) {
}
