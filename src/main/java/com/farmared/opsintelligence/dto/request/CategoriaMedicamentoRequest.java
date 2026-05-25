package com.farmared.opsintelligence.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaMedicamentoRequest(

        @NotBlank(message = "El codigo de la categoria es obligatorio")
        @Size(max = 50, message = "El codigo no puede superar los 50 caracteres")
        String codigo,

        @NotBlank(message = "El nombre de la categoria es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripcion no puede superar los 500 caracteres")
        String descripcion,

        Boolean activo
) {
}
