package com.farmared.opsintelligence.service;

import com.farmared.opsintelligence.dto.request.MovimientoInventarioRequest;
import com.farmared.opsintelligence.dto.response.MovimientoInventarioResponse;

import java.util.List;

public interface MovimientoInventarioService {

    MovimientoInventarioResponse registrarMovimiento(MovimientoInventarioRequest request);

    MovimientoInventarioResponse consultarPorId(Long id);

    List<MovimientoInventarioResponse> consultarKardexPorInventario(Long inventarioId);
}