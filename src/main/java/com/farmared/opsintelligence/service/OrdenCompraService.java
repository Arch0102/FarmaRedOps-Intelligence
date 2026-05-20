package com.farmared.opsintelligence.service;

import com.farmared.opsintelligence.dto.request.OrdenCompraRequest;
import com.farmared.opsintelligence.dto.response.OrdenCompraResponse;
import com.farmared.opsintelligence.entity.enums.EstadoOrdenCompra;

import java.util.List;

public interface OrdenCompraService {

    OrdenCompraResponse crearOrdenCompra(OrdenCompraRequest request);

    OrdenCompraResponse consultarPorId(Long id);

    List<OrdenCompraResponse> listarTodas();

    List<OrdenCompraResponse> listarPorEstado(EstadoOrdenCompra estado);

    OrdenCompraResponse cambiarEstado(Long id, EstadoOrdenCompra nuevoEstado);
}