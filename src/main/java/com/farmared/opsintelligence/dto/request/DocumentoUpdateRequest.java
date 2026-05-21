package com.farmared.opsintelligence.dto.request;

import com.farmared.opsintelligence.entity.enums.EstadoDocumento;
import com.farmared.opsintelligence.entity.enums.TipoDocumento;
import jakarta.validation.constraints.Size;

public record DocumentoUpdateRequest(

        TipoDocumento tipoDocumento,

        EstadoDocumento estadoDocumento,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        @Size(max = 100, message = "El módulo de referencia no puede superar los 100 caracteres")
        String moduloReferencia,

        Long referenciaId
) {
}