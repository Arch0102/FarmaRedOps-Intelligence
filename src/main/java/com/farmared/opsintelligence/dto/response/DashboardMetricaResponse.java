package com.farmared.opsintelligence.dto.response;

import com.farmared.opsintelligence.entity.enums.TipoMetricaDashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DashboardMetricaResponse(
        Long id,
        TipoMetricaDashboard tipoMetrica,
        BigDecimal valor,
        String unidad,
        String descripcion,
        LocalDateTime fechaCalculo,
        Long medicamentoId,
        String medicamentoNombre,
        Long centroDistribucionId,
        String centroDistribucionNombre
) {
}