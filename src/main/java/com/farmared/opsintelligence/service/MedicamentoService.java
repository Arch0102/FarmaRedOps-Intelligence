package com.farmared.opsintelligence.service;

import com.farmared.opsintelligence.dto.request.MedicamentoRequest;
import com.farmared.opsintelligence.dto.response.MedicamentoResponse;

import java.util.List;

public interface MedicamentoService {

    List<MedicamentoResponse> listar();

    MedicamentoResponse consultarPorId(Long id);

    MedicamentoResponse crear(MedicamentoRequest request);

    MedicamentoResponse actualizar(Long id, MedicamentoRequest request);

    void eliminar(Long id);
}