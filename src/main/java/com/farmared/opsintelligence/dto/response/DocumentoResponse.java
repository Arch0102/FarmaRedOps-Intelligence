package com.farmared.opsintelligence.dto.response;

import com.farmared.opsintelligence.entity.enums.EstadoDocumento;
import com.farmared.opsintelligence.entity.enums.TipoDocumento;

import java.time.LocalDateTime;

public record DocumentoResponse(
        Long id,
        String nombreOriginal,
        String nombreAlmacenado,
        String contentType,
        Long tamanoBytes,
        String rutaArchivo,
        TipoDocumento tipoDocumento,
        EstadoDocumento estadoDocumento,
        String descripcion,
        String moduloReferencia,
        Long referenciaId,
        String usuarioCarga,
        LocalDateTime fechaCarga,
        Boolean activo
) {
}