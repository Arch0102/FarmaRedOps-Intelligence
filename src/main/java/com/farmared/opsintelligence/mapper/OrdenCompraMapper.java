package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.response.OrdenCompraResponse;
import com.farmared.opsintelligence.entity.OrdenCompra;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = DetalleOrdenMapper.class)
public interface OrdenCompraMapper {

    @Mapping(source = "proveedor.id", target = "proveedorId")
    @Mapping(source = "proveedor.nit", target = "proveedorNit")
    @Mapping(source = "proveedor.nombre", target = "proveedorNombre")
    OrdenCompraResponse toResponse(OrdenCompra ordenCompra);
}
