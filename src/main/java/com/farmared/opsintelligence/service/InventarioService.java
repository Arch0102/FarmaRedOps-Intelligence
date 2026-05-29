package com.farmared.opsintelligence.service;

import com.farmared.opsintelligence.dto.response.InventarioResponse;
import com.farmared.opsintelligence.dto.response.InventarioResumenResponse;

import java.util.List;

public interface InventarioService {

    List<InventarioResponse> listar();

    InventarioResponse consultarPorId(Long id);

    InventarioResumenResponse obtenerResumen();
}
