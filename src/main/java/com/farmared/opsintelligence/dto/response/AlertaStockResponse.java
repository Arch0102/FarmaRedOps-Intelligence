package com.farmared.opsintelligence.dto.response;

import com.farmared.opsintelligence.entity.enums.EstadoAlerta;
import enums.TipoAlerta;

import java.time.LocalDateTime;

public record AlertaStockResponse(
        Long id,
        TipoAlerta tipoAlerta,
        EstadoAlerta estadoAlerta,
        String mensaje,

        Long medicamentoId,
        String medicamentoCodigo,
        String medicamentoNombre,

        Long centroDistribucionId,
        String centroDistribucionNombre,

        Long loteMedicamentoId,
        String numeroLote,

        LocalDateTime fechaGeneracion,
        LocalDateTime fechaResolucion
) {
}