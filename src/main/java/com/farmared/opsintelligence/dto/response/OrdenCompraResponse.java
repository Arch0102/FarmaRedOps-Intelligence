package com.farmared.opsintelligence.dto.response;

import com.farmared.opsintelligence.entity.enums.EstadoOrdenCompra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenCompraResponse(
        Long id,
        String codigo,
        LocalDate fechaOrden,
        LocalDate fechaEstimadaEntrega,
        LocalDate fechaRecepcion,
        EstadoOrdenCompra estado,
        BigDecimal total,
        String observacion,

        Long proveedorId,
        String proveedorNit,
        String proveedorNombre,

        List<DetalleOrdenResponse> detalles,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}