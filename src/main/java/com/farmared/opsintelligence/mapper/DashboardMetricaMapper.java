package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.response.DashboardMetricaResponse;
import com.farmared.opsintelligence.entity.DashboardMetrica;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DashboardMetricaMapper {

    @Mapping(source = "medicamento.id", target = "medicamentoId")
    @Mapping(source = "medicamento.nombre", target = "medicamentoNombre")
    @Mapping(source = "centroDistribucion.id", target = "centroDistribucionId")
    @Mapping(source = "centroDistribucion.nombre", target = "centroDistribucionNombre")
    DashboardMetricaResponse toResponse(DashboardMetrica metrica);
}
