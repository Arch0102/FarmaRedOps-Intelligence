package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.request.ProveedorRequest;
import com.farmared.opsintelligence.dto.response.ProveedorResponse;
import com.farmared.opsintelligence.entity.Proveedor;
import com.farmared.opsintelligence.exception.BusinessRuleException;
import com.farmared.opsintelligence.exception.ResourceNotFoundException;
import com.farmared.opsintelligence.mapper.ProveedorMapper;
import com.farmared.opsintelligence.repository.ProveedorRepository;
import com.farmared.opsintelligence.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;

    @Override
    public ProveedorResponse crearProveedor(ProveedorRequest request) {
        if (proveedorRepository.existsByNit(request.nit())) {
            throw new BusinessRuleException("Ya existe un proveedor con el NIT: " + request.nit());
        }

        Proveedor proveedor = proveedorMapper.toEntity(request);
        proveedor.setActivo(request.activo() != null ? request.activo() : true);

        return proveedorMapper.toResponse(proveedorRepository.save(proveedor));
    }

    @Override
    public ProveedorResponse actualizarProveedor(Long id, ProveedorRequest request) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));

        proveedorRepository.findByNit(request.nit())
                .filter(proveedorExistente -> !proveedorExistente.getId().equals(id))
                .ifPresent(proveedorExistente -> {
                    throw new BusinessRuleException("Ya existe otro proveedor con el NIT: " + request.nit());
                });

        proveedor.setNit(request.nit());
        proveedor.setNombre(request.nombre());
        proveedor.setTelefono(request.telefono());
        proveedor.setCorreo(request.correo());
        proveedor.setDireccion(request.direccion());
        proveedor.setActivo(request.activo() != null ? request.activo() : proveedor.getActivo());

        return proveedorMapper.toResponse(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponse consultarPorId(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));

        return proveedorMapper.toResponse(proveedor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorResponse> listarTodos() {
        return proveedorRepository.findAll()
                .stream()
                .map(proveedorMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorResponse> listarActivos() {
        return proveedorRepository.findByActivoTrue()
                .stream()
                .map(proveedorMapper::toResponse)
                .toList();
    }

    @Override
    public void desactivarProveedor(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));

        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
    }
}
