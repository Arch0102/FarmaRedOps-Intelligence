package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.response.MedicamentoProveedorResponse;
import com.farmared.opsintelligence.entity.MedicamentoProveedor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MedicamentoProveedorMapper {

    @Mapping(source = "medicamento.id", target = "medicamentoId")
    @Mapping(source = "medicamento.codigo", target = "medicamentoCodigo")
    @Mapping(source = "medicamento.nombre", target = "medicamentoNombre")
    @Mapping(source = "proveedor.id", target = "proveedorId")
    @Mapping(source = "proveedor.nit", target = "proveedorNit")
    @Mapping(source = "proveedor.nombre", target = "proveedorNombre")
    MedicamentoProveedorResponse toResponse(MedicamentoProveedor medicamentoProveedor);
}
