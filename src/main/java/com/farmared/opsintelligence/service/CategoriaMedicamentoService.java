package com.farmared.opsintelligence.service;

import com.farmared.opsintelligence.dto.request.CategoriaMedicamentoRequest;
import com.farmared.opsintelligence.dto.response.CategoriaMedicamentoResponse;

import java.util.List;

public interface CategoriaMedicamentoService {

    List<CategoriaMedicamentoResponse> listar();

    CategoriaMedicamentoResponse consultarPorId(Long id);

    CategoriaMedicamentoResponse crear(CategoriaMedicamentoRequest request);

    CategoriaMedicamentoResponse actualizar(Long id, CategoriaMedicamentoRequest request);

    void eliminar(Long id);
}