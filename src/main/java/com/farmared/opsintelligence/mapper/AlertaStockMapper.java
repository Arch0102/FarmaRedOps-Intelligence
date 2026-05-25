package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.response.AlertaStockResponse;
import com.farmared.opsintelligence.entity.AlertaStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AlertaStockMapper {

    @Mapping(source = "medicamento.id", target = "medicamentoId")
    @Mapping(source = "medicamento.codigo", target = "medicamentoCodigo")
    @Mapping(source = "medicamento.nombre", target = "medicamentoNombre")
    @Mapping(source = "centroDistribucion.id", target = "centroDistribucionId")
    @Mapping(source = "centroDistribucion.nombre", target = "centroDistribucionNombre")
    @Mapping(source = "loteMedicamento.id", target = "loteMedicamentoId")
    @Mapping(source = "loteMedicamento.numeroLote", target = "numeroLote")
    AlertaStockResponse toResponse(AlertaStock alertaStock);
}
