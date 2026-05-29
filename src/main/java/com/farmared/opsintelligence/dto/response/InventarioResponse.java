package com.farmared.opsintelligence.dto.response;

import java.time.LocalDateTime;

public record InventarioResponse(
        Long id,

        Long medicamentoId,
        String medicamentoCodigo,
        String medicamentoNombre,

        Long centroDistribucionId,
        String centroDistribucionNombre,

        Integer stockActual,
        Integer stockReservado,
        Integer stockDisponible,
        Integer stockMinimo,
        Integer puntoReorden,
        EstadoStock estadoStock,

        LocalDateTime fechaUltimaActualizacion
) {
}
