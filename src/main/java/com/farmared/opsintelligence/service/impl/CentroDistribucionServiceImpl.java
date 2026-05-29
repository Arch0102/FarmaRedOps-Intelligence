package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.request.CentroDistribucionRequest;
import com.farmared.opsintelligence.dto.response.CentroDistribucionResponse;
import com.farmared.opsintelligence.entity.CentroDistribucion;
import com.farmared.opsintelligence.exception.DuplicateResourceException;
import com.farmared.opsintelligence.exception.ResourceNotFoundException;
import com.farmared.opsintelligence.mapper.CentroDistribucionMapper;
import com.farmared.opsintelligence.repository.CentroDistribucionRepository;
import com.farmared.opsintelligence.service.CentroDistribucionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CentroDistribucionServiceImpl implements CentroDistribucionService {

    private final CentroDistribucionRepository centroDistribucionRepository;
    private final CentroDistribucionMapper centroDistribucionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CentroDistribucionResponse> listar() {
        return centroDistribucionRepository.findByActivoTrue()
                .stream()
                .map(centroDistribucionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CentroDistribucionResponse consultarPorId(Long id) {
        CentroDistribucion centro = buscarCentroPorId(id);
        return centroDistribucionMapper.toResponse(centro);
    }

    @Override
    public CentroDistribucionResponse crear(CentroDistribucionRequest request) {
        if (centroDistribucionRepository.existsByCodigo(request.codigo())) {
            throw new DuplicateResourceException("Ya existe un centro de distribución con el código: " + request.codigo());
        }

        CentroDistribucion centro = centroDistribucionMapper.toEntity(request);
        if (centro.getActivo() == null) {
            centro.setActivo(true);
        }

        CentroDistribucion centroGuardado = centroDistribucionRepository.save(centro);

        return centroDistribucionMapper.toResponse(centroGuardado);
    }

    @Override
    public CentroDistribucionResponse actualizar(Long id, CentroDistribucionRequest request) {
        CentroDistribucion centro = buscarCentroPorId(id);

        centroDistribucionRepository.findByCodigo(request.codigo())
                .filter(centroExistente -> !centroExistente.getId().equals(id))
                .ifPresent(centroExistente -> {
                    throw new DuplicateResourceException("Ya existe un centro de distribución con el código: " + request.codigo());
                });

        centro.setCodigo(request.codigo());
        centro.setNombre(request.nombre());
        centro.setDireccion(request.direccion());
        centro.setCiudad(request.ciudad());

        if (request.activo() != null) {
            centro.setActivo(request.activo());
        }

        CentroDistribucion centroActualizado = centroDistribucionRepository.save(centro);

        return centroDistribucionMapper.toResponse(centroActualizado);
    }

    @Override
    public void eliminar(Long id) {
        CentroDistribucion centro = buscarCentroPorId(id);

        // Eliminación lógica para no romper relaciones con inventarios, alertas o métricas.
        centro.setActivo(false);
        centroDistribucionRepository.save(centro);
    }

    private CentroDistribucion buscarCentroPorId(Long id) {
        return centroDistribucionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centro de distribución no encontrado con id: " + id));
    }

}
