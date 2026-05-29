package com.farmared.opsintelligence.service;

import com.farmared.opsintelligence.dto.response.LoteMedicamentoResponse;

import java.util.List;

public interface LoteMedicamentoService {

    List<LoteMedicamentoResponse> listar();

    List<LoteMedicamentoResponse> listarPorMedicamento(Long medicamentoId);
}
