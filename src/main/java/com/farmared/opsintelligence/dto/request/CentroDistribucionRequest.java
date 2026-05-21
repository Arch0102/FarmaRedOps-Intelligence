package com.farmared.opsintelligence.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CentroDistribucionRequest(

        @NotBlank(message = "El código del centro de distribución es obligatorio")
        @Size(max = 50, message = "El código no puede superar los 50 caracteres")
        String codigo,

        @NotBlank(message = "El nombre del centro de distribución es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String nombre,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
        String direccion,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 100, message = "La ciudad no puede superar los 100 caracteres")
        String ciudad,

        Boolean activo
) {
}