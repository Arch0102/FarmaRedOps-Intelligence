package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.request.DetalleOrdenRequest;
import com.farmared.opsintelligence.dto.request.OrdenCompraRequest;
import com.farmared.opsintelligence.dto.response.OrdenCompraResponse;
import com.farmared.opsintelligence.entity.DetalleOrden;
import com.farmared.opsintelligence.entity.Medicamento;
import com.farmared.opsintelligence.entity.OrdenCompra;
import com.farmared.opsintelligence.entity.Proveedor;
import com.farmared.opsintelligence.entity.enums.EstadoOrdenCompra;
import com.farmared.opsintelligence.exception.BusinessRuleException;
import com.farmared.opsintelligence.exception.ResourceNotFoundException;
import com.farmared.opsintelligence.mapper.OrdenCompraMapper;
import com.farmared.opsintelligence.repository.DetalleOrdenRepository;
import com.farmared.opsintelligence.repository.MedicamentoRepository;
import com.farmared.opsintelligence.repository.OrdenCompraRepository;
import com.farmared.opsintelligence.repository.ProveedorRepository;
import com.farmared.opsintelligence.service.OrdenCompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrdenCompraServiceImpl implements OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final DetalleOrdenRepository detalleOrdenRepository;
    private final ProveedorRepository proveedorRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final OrdenCompraMapper ordenCompraMapper;

    @Override
    public OrdenCompraResponse crearOrdenCompra(OrdenCompraRequest request) {
        if (ordenCompraRepository.existsByCodigo(request.codigo())) {
            throw new BusinessRuleException("Ya existe una orden de compra con el codigo: " + request.codigo());
        }

        Proveedor proveedor = proveedorRepository.findById(request.proveedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + request.proveedorId()));

        BigDecimal total = calcularTotal(request.detalles());

        OrdenCompra ordenCompra = new OrdenCompra();
        ordenCompra.setCodigo(request.codigo());
        ordenCompra.setFechaOrden(LocalDate.now());
        ordenCompra.setFechaEstimadaEntrega(request.fechaEstimadaEntrega());
        ordenCompra.setEstado(EstadoOrdenCompra.PENDIENTE);
        ordenCompra.setTotal(total);
        ordenCompra.setObservacion(request.observacion());
        ordenCompra.setProveedor(proveedor);

        OrdenCompra ordenGuardada = ordenCompraRepository.save(ordenCompra);

        List<DetalleOrden> detalles = request.detalles()
                .stream()
                .map(detalleRequest -> crearDetalleOrden(detalleRequest, ordenGuardada))
                .toList();

        detalleOrdenRepository.saveAll(detalles);
        ordenGuardada.setDetalles(detalles);

        return ordenCompraMapper.toResponse(ordenGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenCompraResponse consultarPorId(Long id) {
        OrdenCompra ordenCompra = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de compra no encontrada con ID: " + id));

        return toResponse(ordenCompra);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompraResponse> listarTodas() {
        return ordenCompraRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompraResponse> listarPorEstado(EstadoOrdenCompra estado) {
        return ordenCompraRepository.findByEstado(estado)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public OrdenCompraResponse cambiarEstado(Long id, EstadoOrdenCompra nuevoEstado) {
        OrdenCompra ordenCompra = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de compra no encontrada con ID: " + id));

        validarCambioEstado(ordenCompra.getEstado(), nuevoEstado);

        ordenCompra.setEstado(nuevoEstado);

        if (nuevoEstado == EstadoOrdenCompra.RECIBIDA) {
            ordenCompra.setFechaRecepcion(LocalDate.now());
        }

        return toResponse(ordenCompraRepository.save(ordenCompra));
    }

    private BigDecimal calcularTotal(List<DetalleOrdenRequest> detalles) {
        return detalles.stream()
                .map(detalle -> detalle.precioUnitario().multiply(BigDecimal.valueOf(detalle.cantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private DetalleOrden crearDetalleOrden(DetalleOrdenRequest request, OrdenCompra ordenCompra) {
        Medicamento medicamento = medicamentoRepository.findById(request.medicamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado con ID: " + request.medicamentoId()));

        BigDecimal subtotal = request.precioUnitario().multiply(BigDecimal.valueOf(request.cantidad()));

        DetalleOrden detalleOrden = new DetalleOrden();
        detalleOrden.setOrdenCompra(ordenCompra);
        detalleOrden.setMedicamento(medicamento);
        detalleOrden.setCantidad(request.cantidad());
        detalleOrden.setPrecioUnitario(request.precioUnitario());
        detalleOrden.setSubtotal(subtotal);

        return detalleOrden;
    }

    private void validarCambioEstado(EstadoOrdenCompra estadoActual, EstadoOrdenCompra nuevoEstado) {
        if (estadoActual == EstadoOrdenCompra.CANCELADA) {
            throw new BusinessRuleException("No se puede cambiar el estado de una orden cancelada");
        }

        if (estadoActual == EstadoOrdenCompra.RECIBIDA) {
            throw new BusinessRuleException("No se puede cambiar el estado de una orden recibida");
        }

        if (nuevoEstado == EstadoOrdenCompra.BORRADOR) {
            throw new BusinessRuleException("No se puede regresar una orden a estado BORRADOR");
        }
    }

    private OrdenCompraResponse toResponse(OrdenCompra ordenCompra) {
        List<DetalleOrden> detalles = detalleOrdenRepository.findByOrdenCompraId(ordenCompra.getId());
        ordenCompra.setDetalles(detalles);
        return ordenCompraMapper.toResponse(ordenCompra);
    }
}
