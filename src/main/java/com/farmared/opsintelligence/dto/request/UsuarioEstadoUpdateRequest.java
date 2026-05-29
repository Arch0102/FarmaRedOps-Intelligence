package com.farmared.opsintelligence.dto.request;

import jakarta.validation.constraints.NotNull;

public record UsuarioEstadoUpdateRequest(
        @NotNull(message = "El estado activo es obligatorio")
        Boolean activo
) {
}
