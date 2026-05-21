package com.farmared.opsintelligence.dto.request;

import com.farmared.opsintelligence.entity.enums.TipoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DocumentoUploadRequest(

        @NotNull(message = "El tipo de documento es obligatorio")
        TipoDocumento tipoDocumento,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        @Size(max = 100, message = "El módulo de referencia no puede superar los 100 caracteres")
        String moduloReferencia,

        Long referenciaId,

        @NotBlank(message = "El usuario de carga es obligatorio")
        @Size(max = 150, message = "El usuario de carga no puede superar los 150 caracteres")
        String usuarioCarga
) {
}
