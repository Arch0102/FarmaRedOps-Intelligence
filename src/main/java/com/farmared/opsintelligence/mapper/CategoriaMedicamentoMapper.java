package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.request.CategoriaMedicamentoRequest;
import com.farmared.opsintelligence.dto.response.CategoriaMedicamentoResponse;
import com.farmared.opsintelligence.entity.CategoriaMedicamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoriaMedicamentoMapper {

    CategoriaMedicamentoResponse toResponse(CategoriaMedicamento categoria);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "medicamentos", ignore = true)
    CategoriaMedicamento toEntity(CategoriaMedicamentoRequest request);
}
