package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.response.DetalleOrdenResponse;
import com.farmared.opsintelligence.entity.DetalleOrden;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DetalleOrdenMapper {

    @Mapping(source = "medicamento.id", target = "medicamentoId")
    @Mapping(source = "medicamento.codigo", target = "medicamentoCodigo")
    @Mapping(source = "medicamento.nombre", target = "medicamentoNombre")
    DetalleOrdenResponse toResponse(DetalleOrden detalleOrden);
}
