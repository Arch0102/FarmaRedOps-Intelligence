package com.farmared.opsintelligence.service;

import com.farmared.opsintelligence.dto.response.AlertaStockResponse;
import com.farmared.opsintelligence.entity.Inventario;

import java.util.List;

public interface AlertaStockService {

    List<AlertaStockResponse> listarPendientes();

    AlertaStockResponse resolver(Long id);

    void evaluarInventario(Inventario inventario);

    void evaluarInventarios();
}
