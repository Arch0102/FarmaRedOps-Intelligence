package com.farmared.opsintelligence.dto.request;

import com.farmared.opsintelligence.entity.enums.TipoMovimiento;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MovimientoInventarioRequest(

        @NotNull(message = "El tipo de movimiento es obligatorio")
        TipoMovimiento tipoMovimiento,

        @NotNull(message = "El inventario es obligatorio")
        Long inventarioId,

        @NotNull(message = "El lote del medicamento es obligatorio")
        Long loteMedicamentoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor a cero")
        Integer cantidad,

        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 200, message = "El motivo no puede superar los 200 caracteres")
        String motivo,

        @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
        String observacion,

        @NotBlank(message = "El usuario responsable es obligatorio")
        @Size(max = 150, message = "El usuario responsable no puede superar los 150 caracteres")
        String usuarioResponsable
) {
}