package com.farmared.opsintelligence.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MedicamentoRequest(

        @NotBlank(message = "El código del medicamento es obligatorio")
        @Size(max = 50, message = "El código no puede superar los 50 caracteres")
        String codigo,

        @NotBlank(message = "El nombre del medicamento es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        @NotBlank(message = "El principio activo es obligatorio")
        @Size(max = 150, message = "El principio activo no puede superar los 150 caracteres")
        String principioActivo,

        @NotBlank(message = "La concentración es obligatoria")
        @Size(max = 100, message = "La concentración no puede superar los 100 caracteres")
        String concentracion,

        @NotBlank(message = "La presentación es obligatoria")
        @Size(max = 100, message = "La presentación no puede superar los 100 caracteres")
        String presentacion,

        @NotBlank(message = "La unidad de medida es obligatoria")
        @Size(max = 50, message = "La unidad de medida no puede superar los 50 caracteres")
        String unidadMedida,

        @NotNull(message = "El stock mínimo es obligatorio")
        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        Integer stockMinimo,

        @NotNull(message = "El stock máximo es obligatorio")
        @Min(value = 1, message = "El stock máximo debe ser mayor a cero")
        Integer stockMaximo,

        @NotNull(message = "El punto de reorden es obligatorio")
        @Min(value = 0, message = "El punto de reorden no puede ser negativo")
        Integer puntoReorden,

        Boolean activo,

        @NotNull(message = "La categoría del medicamento es obligatoria")
        Long categoriaMedicamentoId
) {
}