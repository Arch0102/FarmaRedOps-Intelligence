package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.request.MedicamentoRequest;
import com.farmared.opsintelligence.dto.response.MedicamentoResponse;
import com.farmared.opsintelligence.entity.CategoriaMedicamento;
import com.farmared.opsintelligence.entity.Medicamento;
import com.farmared.opsintelligence.exception.BadRequestException;
import com.farmared.opsintelligence.exception.DuplicateResourceException;
import com.farmared.opsintelligence.exception.ResourceNotFoundException;
import com.farmared.opsintelligence.repository.CategoriaMedicamentoRepository;
import com.farmared.opsintelligence.repository.MedicamentoRepository;
import com.farmared.opsintelligence.service.MedicamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicamentoServiceImpl implements MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final CategoriaMedicamentoRepository categoriaMedicamentoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MedicamentoResponse> listar() {
        return medicamentoRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicamentoResponse consultarPorId(Long id) {
        Medicamento medicamento = buscarMedicamentoPorId(id);
        return mapToResponse(medicamento);
    }

    @Override
    public MedicamentoResponse crear(MedicamentoRequest request) {
        validarStock(request.stockMinimo(), request.stockMaximo(), request.puntoReorden());

        if (medicamentoRepository.existsByCodigo(request.codigo())) {
            throw new DuplicateResourceException("Ya existe un medicamento con el código: " + request.codigo());
        }

        CategoriaMedicamento categoria = buscarCategoriaPorId(request.categoriaMedicamentoId());

        Medicamento medicamento = new Medicamento();
        medicamento.setCodigo(request.codigo());
        medicamento.setNombre(request.nombre());
        medicamento.setDescripcion(request.descripcion());
        medicamento.setPrincipioActivo(request.principioActivo());
        medicamento.setConcentracion(request.concentracion());
        medicamento.setPresentacion(request.presentacion());
        medicamento.setUnidadMedida(request.unidadMedida());
        medicamento.setStockMinimo(request.stockMinimo());
        medicamento.setStockMaximo(request.stockMaximo());
        medicamento.setPuntoReorden(request.puntoReorden());
        medicamento.setActivo(request.activo() != null ? request.activo() : true);
        medicamento.setCategoriaMedicamento(categoria);

        Medicamento medicamentoGuardado = medicamentoRepository.save(medicamento);

        return mapToResponse(medicamentoGuardado);
    }

    @Override
    public MedicamentoResponse actualizar(Long id, MedicamentoRequest request) {
        validarStock(request.stockMinimo(), request.stockMaximo(), request.puntoReorden());

        Medicamento medicamento = buscarMedicamentoPorId(id);

        medicamentoRepository.findByCodigo(request.codigo())
                .filter(medicamentoExistente -> !medicamentoExistente.getId().equals(id))
                .ifPresent(medicamentoExistente -> {
                    throw new DuplicateResourceException("Ya existe un medicamento con el código: " + request.codigo());
                });

        CategoriaMedicamento categoria = buscarCategoriaPorId(request.categoriaMedicamentoId());

        medicamento.setCodigo(request.codigo());
        medicamento.setNombre(request.nombre());
        medicamento.setDescripcion(request.descripcion());
        medicamento.setPrincipioActivo(request.principioActivo());
        medicamento.setConcentracion(request.concentracion());
        medicamento.setPresentacion(request.presentacion());
        medicamento.setUnidadMedida(request.unidadMedida());
        medicamento.setStockMinimo(request.stockMinimo());
        medicamento.setStockMaximo(request.stockMaximo());
        medicamento.setPuntoReorden(request.puntoReorden());
        medicamento.setCategoriaMedicamento(categoria);

        if (request.activo() != null) {
            medicamento.setActivo(request.activo());
        }

        Medicamento medicamentoActualizado = medicamentoRepository.save(medicamento);

        return mapToResponse(medicamentoActualizado);
    }

    @Override
    public void eliminar(Long id) {
        Medicamento medicamento = buscarMedicamentoPorId(id);

        // Eliminación lógica para no romper relaciones con inventario, lotes, movimientos o alertas.
        medicamento.setActivo(false);
        medicamentoRepository.save(medicamento);
    }

    private Medicamento buscarMedicamentoPorId(Long id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado con id: " + id));
    }

    private CategoriaMedicamento buscarCategoriaPorId(Long id) {
        return categoriaMedicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría de medicamento no encontrada con id: " + id));
    }

    private void validarStock(Integer stockMinimo, Integer stockMaximo, Integer puntoReorden) {
        if (stockMinimo > stockMaximo) {
            throw new BadRequestException("El stock mínimo no puede ser mayor que el stock máximo");
        }

        if (puntoReorden > stockMaximo) {
            throw new BadRequestException("El punto de reorden no puede ser mayor que el stock máximo");
        }
    }

    private MedicamentoResponse mapToResponse(Medicamento medicamento) {
        return new MedicamentoResponse(
                medicamento.getId(),
                medicamento.getCodigo(),
                medicamento.getNombre(),
                medicamento.getDescripcion(),
                medicamento.getPrincipioActivo(),
                medicamento.getConcentracion(),
                medicamento.getPresentacion(),
                medicamento.getUnidadMedida(),
                medicamento.getStockMinimo(),
                medicamento.getStockMaximo(),
                medicamento.getPuntoReorden(),
                medicamento.getActivo(),
                medicamento.getCategoriaMedicamento().getId(),
                medicamento.getCategoriaMedicamento().getNombre(),
                medicamento.getCreatedAt(),
                medicamento.getUpdatedAt()
        );
    }
}