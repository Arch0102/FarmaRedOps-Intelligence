package com.farmared.opsintelligence.dto.response;

import com.farmared.opsintelligence.entity.enums.EstadoOrdenCompra;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardResumenResponse(
        Long totalMedicamentosActivos,
        Long totalProveedoresActivos,
        Long totalOrdenesCompra,
        Map<EstadoOrdenCompra, Long> ordenesPorEstado,
        Long totalInventarios,
        Long totalStockCritico,
        Long totalLotesProximosVencer,
        Long totalOrdenesPendientes,
        Long totalAlertasPendientes,
        BigDecimal valorTotalOrdenesPendientes,
        Integer cantidadTotalInventario,
        List<MovimientoInventarioResponse> movimientosRecientes
) {
}
