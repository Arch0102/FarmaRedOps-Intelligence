package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.request.MedicamentoRequest;
import com.farmared.opsintelligence.dto.response.MedicamentoResponse;
import com.farmared.opsintelligence.entity.Medicamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicamentoMapper {

    @Mapping(source = "categoriaMedicamento.id", target = "categoriaMedicamentoId")
    @Mapping(source = "categoriaMedicamento.nombre", target = "categoriaMedicamentoNombre")
    MedicamentoResponse toResponse(Medicamento medicamento);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "categoriaMedicamento", ignore = true)
    @Mapping(target = "inventarios", ignore = true)
    @Mapping(target = "lotes", ignore = true)
    @Mapping(target = "alertasStock", ignore = true)
    @Mapping(target = "medicamentosProveedor", ignore = true)
    @Mapping(target = "detallesOrden", ignore = true)
    @Mapping(target = "metricasDashboard", ignore = true)
    Medicamento toEntity(MedicamentoRequest request);
}
