package com.farmared.opsintelligence.service;

import com.farmared.opsintelligence.dto.request.ProveedorRequest;
import com.farmared.opsintelligence.dto.response.ProveedorResponse;

import java.util.List;

public interface ProveedorService {

    ProveedorResponse crearProveedor(ProveedorRequest request);

    ProveedorResponse actualizarProveedor(Long id, ProveedorRequest request);

    ProveedorResponse consultarPorId(Long id);

    List<ProveedorResponse> listarTodos();

    List<ProveedorResponse> listarActivos();

    void desactivarProveedor(Long id);
}