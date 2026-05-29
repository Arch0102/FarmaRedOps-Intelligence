package com.farmared.opsintelligence.service;

import com.farmared.opsintelligence.dto.request.CentroDistribucionRequest;
import com.farmared.opsintelligence.dto.response.CentroDistribucionResponse;

import java.util.List;

public interface CentroDistribucionService {

    List<CentroDistribucionResponse> listar();

    CentroDistribucionResponse consultarPorId(Long id);

    CentroDistribucionResponse crear(CentroDistribucionRequest request);

    CentroDistribucionResponse actualizar(Long id, CentroDistribucionRequest request);

    void eliminar(Long id);
}