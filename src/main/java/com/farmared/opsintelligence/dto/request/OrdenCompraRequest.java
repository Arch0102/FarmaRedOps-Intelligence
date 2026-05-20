package com.farmared.opsintelligence.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record OrdenCompraRequest(

        @NotBlank(message = "El código de la orden es obligatorio")
        @Size(max = 50, message = "El código no puede superar los 50 caracteres")
        String codigo,

        @NotNull(message = "El proveedor es obligatorio")
        Long proveedorId,

        LocalDate fechaEstimadaEntrega,

        @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
        String observacion,

        @NotEmpty(message = "La orden debe tener al menos un detalle")
        List<@Valid DetalleOrdenRequest> detalles
) {
}