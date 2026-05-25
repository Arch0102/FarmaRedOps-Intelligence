package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.response.LoteMedicamentoResponse;
import com.farmared.opsintelligence.entity.LoteMedicamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoteMedicamentoMapper {

    @Mapping(source = "medicamento.id", target = "medicamentoId")
    @Mapping(source = "medicamento.codigo", target = "medicamentoCodigo")
    @Mapping(source = "medicamento.nombre", target = "medicamentoNombre")
    LoteMedicamentoResponse toResponse(LoteMedicamento loteMedicamento);
}
