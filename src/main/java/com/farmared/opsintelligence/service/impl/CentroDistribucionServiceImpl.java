package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.request.CentroDistribucionRequest;
import com.farmared.opsintelligence.dto.response.CentroDistribucionResponse;
import com.farmared.opsintelligence.entity.CentroDistribucion;
import com.farmared.opsintelligence.exception.DuplicateResourceException;
import com.farmared.opsintelligence.exception.ResourceNotFoundException;
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

    @Override
    @Transactional(readOnly = true)
    public List<CentroDistribucionResponse> listar() {
        return centroDistribucionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CentroDistribucionResponse consultarPorId(Long id) {
        CentroDistribucion centro = buscarCentroPorId(id);
        return mapToResponse(centro);
    }

    @Override
    public CentroDistribucionResponse crear(CentroDistribucionRequest request) {
        if (centroDistribucionRepository.existsByCodigo(request.codigo())) {
            throw new DuplicateResourceException("Ya existe un centro de distribución con el código: " + request.codigo());
        }

        CentroDistribucion centro = new CentroDistribucion();
        centro.setCodigo(request.codigo());
        centro.setNombre(request.nombre());
        centro.setDireccion(request.direccion());
        centro.setCiudad(request.ciudad());
        centro.setActivo(request.activo() != null ? request.activo() : true);

        CentroDistribucion centroGuardado = centroDistribucionRepository.save(centro);

        return mapToResponse(centroGuardado);
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

        return mapToResponse(centroActualizado);
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

    private CentroDistribucionResponse mapToResponse(CentroDistribucion centro) {
        return new CentroDistribucionResponse(
                centro.getId(),
                centro.getCodigo(),
                centro.getNombre(),
                centro.getDireccion(),
                centro.getCiudad(),
                centro.getActivo(),
                centro.getCreatedAt(),
                centro.getUpdatedAt()
        );
    }
}