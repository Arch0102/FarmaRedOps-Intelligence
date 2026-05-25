package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.response.MovimientoInventarioResponse;
import com.farmared.opsintelligence.entity.MovimientoInventario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MovimientoInventarioMapper {

    @Mapping(source = "inventario.id", target = "inventarioId")
    @Mapping(source = "inventario.medicamento.id", target = "medicamentoId")
    @Mapping(source = "inventario.medicamento.codigo", target = "medicamentoCodigo")
    @Mapping(source = "inventario.medicamento.nombre", target = "medicamentoNombre")
    @Mapping(source = "loteMedicamento.id", target = "loteMedicamentoId")
    @Mapping(source = "loteMedicamento.numeroLote", target = "numeroLote")
    @Mapping(source = "inventario.centroDistribucion.id", target = "centroDistribucionId")
    @Mapping(source = "inventario.centroDistribucion.nombre", target = "centroDistribucionNombre")
    MovimientoInventarioResponse toResponse(MovimientoInventario movimientoInventario);
}
