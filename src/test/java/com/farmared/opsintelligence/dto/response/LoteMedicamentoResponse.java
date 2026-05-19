package com.farmared.opsintelligence.dto.response;

import com.farmared.opsintelligence.entity.enums.EstadoLote;

import java.time.LocalDate;

public record LoteMedicamentoResponse(
        Long id,
        String numeroLote,

        Long medicamentoId,
        String medicamentoCodigo,
        String medicamentoNombre,

        LocalDate fechaFabricacion,
        LocalDate fechaVencimiento,

        Integer cantidadInicial,
        Integer cantidadActual,

        EstadoLote estado
) {
}
