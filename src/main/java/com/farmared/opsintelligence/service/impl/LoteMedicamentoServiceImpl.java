package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.response.LoteMedicamentoResponse;
import com.farmared.opsintelligence.entity.LoteMedicamento;
import com.farmared.opsintelligence.entity.Medicamento;
import com.farmared.opsintelligence.repository.LoteMedicamentoRepository;
import com.farmared.opsintelligence.service.LoteMedicamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoteMedicamentoServiceImpl implements LoteMedicamentoService {

    private final LoteMedicamentoRepository loteMedicamentoRepository;

    @Override
    public List<LoteMedicamentoResponse> listar() {
        return loteMedicamentoRepository.findAllWithDetails()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<LoteMedicamentoResponse> listarPorMedicamento(Long medicamentoId) {
        return loteMedicamentoRepository.findByMedicamentoIdWithDetails(medicamentoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private LoteMedicamentoResponse toResponse(LoteMedicamento lote) {
        Medicamento medicamento = lote.getMedicamento();

        return new LoteMedicamentoResponse(
                lote.getId(),
                medicamento != null ? medicamento.getId() : null,
                medicamento != null ? medicamento.getCodigo() : null,
                medicamento != null ? medicamento.getNombre() : "Sin medicamento",
                lote.getNumeroLote(),
                lote.getFechaFabricacion(),
                lote.getFechaVencimiento(),
                lote.getCantidadInicial(),
                lote.getCantidadActual(),
                lote.getEstado()
        );
    }
}
