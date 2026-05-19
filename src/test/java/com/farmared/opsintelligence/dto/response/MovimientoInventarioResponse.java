package com.farmared.opsintelligence.dto.response;

import com.farmared.opsintelligence.entity.enums.TipoMovimiento;

import java.time.LocalDateTime;

public record MovimientoInventarioResponse(
        Long id,
        TipoMovimiento tipoMovimiento,
        Integer cantidad,
        Integer stockAntes,
        Integer stockDespues,
        String motivo,
        String observacion,
        String usuarioResponsable,
        LocalDateTime fechaMovimiento,

        Long inventarioId,
        Long medicamentoId,
        String medicamentoCodigo,
        String medicamentoNombre,

        Long loteMedicamentoId,
        String numeroLote,

        Long centroDistribucionId,
        String centroDistribucionNombre
) {
}