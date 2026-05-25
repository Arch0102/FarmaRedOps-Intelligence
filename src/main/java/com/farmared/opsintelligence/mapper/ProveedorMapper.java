package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.request.ProveedorRequest;
import com.farmared.opsintelligence.dto.response.ProveedorResponse;
import com.farmared.opsintelligence.entity.Proveedor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProveedorMapper {

    ProveedorResponse toResponse(Proveedor proveedor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "medicamentosProveedor", ignore = true)
    @Mapping(target = "ordenesCompra", ignore = true)
    Proveedor toEntity(ProveedorRequest request);
}
