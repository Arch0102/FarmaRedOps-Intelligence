package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.request.CentroDistribucionRequest;
import com.farmared.opsintelligence.dto.response.CentroDistribucionResponse;
import com.farmared.opsintelligence.entity.CentroDistribucion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CentroDistribucionMapper {

    CentroDistribucionResponse toResponse(CentroDistribucion centro);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "inventarios", ignore = true)
    @Mapping(target = "alertasStock", ignore = true)
    @Mapping(target = "metricasDashboard", ignore = true)
    CentroDistribucion toEntity(CentroDistribucionRequest request);
}
