package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.request.CategoriaMedicamentoRequest;
import com.farmared.opsintelligence.dto.response.CategoriaMedicamentoResponse;
import com.farmared.opsintelligence.entity.CategoriaMedicamento;
import com.farmared.opsintelligence.exception.DuplicateResourceException;
import com.farmared.opsintelligence.exception.ResourceNotFoundException;
import com.farmared.opsintelligence.repository.CategoriaMedicamentoRepository;
import com.farmared.opsintelligence.service.CategoriaMedicamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaMedicamentoServiceImpl implements CategoriaMedicamentoService {

    private final CategoriaMedicamentoRepository categoriaMedicamentoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaMedicamentoResponse> listar() {
        return categoriaMedicamentoRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaMedicamentoResponse consultarPorId(Long id) {
        CategoriaMedicamento categoria = buscarCategoriaPorId(id);
        return mapToResponse(categoria);
    }

    @Override
    public CategoriaMedicamentoResponse crear(CategoriaMedicamentoRequest request) {
        if (categoriaMedicamentoRepository.existsByNombreIgnoreCase(request.nombre())) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre: " + request.nombre());
        }

        CategoriaMedicamento categoria = new CategoriaMedicamento();
        categoria.setNombre(request.nombre());
        categoria.setDescripcion(request.descripcion());
        categoria.setActivo(request.activo() != null ? request.activo() : true);

        CategoriaMedicamento categoriaGuardada = categoriaMedicamentoRepository.save(categoria);

        return mapToResponse(categoriaGuardada);
    }

    @Override
    public CategoriaMedicamentoResponse actualizar(Long id, CategoriaMedicamentoRequest request) {
        CategoriaMedicamento categoria = buscarCategoriaPorId(id);

        categoriaMedicamentoRepository.findByNombreIgnoreCase(request.nombre())
                .filter(categoriaExistente -> !categoriaExistente.getId().equals(id))
                .ifPresent(categoriaExistente -> {
                    throw new DuplicateResourceException("Ya existe una categoría con el nombre: " + request.nombre());
                });

        categoria.setNombre(request.nombre());
        categoria.setDescripcion(request.descripcion());

        if (request.activo() != null) {
            categoria.setActivo(request.activo());
        }

        CategoriaMedicamento categoriaActualizada = categoriaMedicamentoRepository.save(categoria);

        return mapToResponse(categoriaActualizada);
    }

    @Override
    public void eliminar(Long id) {
        CategoriaMedicamento categoria = buscarCategoriaPorId(id);

        // Eliminación lógica para evitar problemas si la categoría ya está asociada a medicamentos.
        categoria.setActivo(false);
        categoriaMedicamentoRepository.save(categoria);
    }

    private CategoriaMedicamento buscarCategoriaPorId(Long id) {
        return categoriaMedicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría de medicamento no encontrada con id: " + id));
    }

    private CategoriaMedicamentoResponse mapToResponse(CategoriaMedicamento categoria) {
        return new CategoriaMedicamentoResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getActivo(),
                categoria.getCreatedAt(),
                categoria.getUpdatedAt()
        );
    }
}