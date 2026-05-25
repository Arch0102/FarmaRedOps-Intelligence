package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.response.InventarioResponse;
import com.farmared.opsintelligence.entity.Inventario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventarioMapper {

    @Mapping(source = "medicamento.id", target = "medicamentoId")
    @Mapping(source = "medicamento.codigo", target = "medicamentoCodigo")
    @Mapping(source = "medicamento.nombre", target = "medicamentoNombre")
    @Mapping(source = "centroDistribucion.id", target = "centroDistribucionId")
    @Mapping(source = "centroDistribucion.nombre", target = "centroDistribucionNombre")
    InventarioResponse toResponse(Inventario inventario);
}
