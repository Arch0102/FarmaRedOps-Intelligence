package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.request.MovimientoInventarioRequest;
import com.farmared.opsintelligence.dto.response.MovimientoInventarioResponse;
import com.farmared.opsintelligence.entity.Inventario;
import com.farmared.opsintelligence.entity.LoteMedicamento;
import com.farmared.opsintelligence.entity.MovimientoInventario;
import com.farmared.opsintelligence.entity.enums.EstadoLote;
import com.farmared.opsintelligence.entity.enums.TipoMovimiento;
import com.farmared.opsintelligence.exception.BusinessRuleException;
import com.farmared.opsintelligence.exception.ResourceNotFoundException;
import com.farmared.opsintelligence.repository.InventarioRepository;
import com.farmared.opsintelligence.repository.LoteMedicamentoRepository;
import com.farmared.opsintelligence.repository.MovimientoInventarioRepository;
import com.farmared.opsintelligence.service.AlertaStockService;
import com.farmared.opsintelligence.service.MovimientoInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

    private final InventarioRepository inventarioRepository;
    private final LoteMedicamentoRepository loteMedicamentoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final AlertaStockService alertaStockService;

    @Override
    public MovimientoInventarioResponse registrarMovimiento(MovimientoInventarioRequest request) {
        Inventario inventario = inventarioRepository.findById(request.inventarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado con ID: " + request.inventarioId()));

        LoteMedicamento lote = loteMedicamentoRepository.findById(request.loteMedicamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + request.loteMedicamentoId()));

        validarLotePerteneceAlMedicamentoDelInventario(inventario, lote);
        validarCantidad(request.cantidad());

        int stockAntes = inventario.getStockActual();
        int stockDespues = calcularStockDespues(stockAntes, request.cantidad(), request.tipoMovimiento());

        validarMovimiento(request.tipoMovimiento(), request.cantidad(), inventario, lote);

        actualizarInventario(inventario, stockDespues);
        actualizarLote(lote, request.tipoMovimiento(), request.cantidad());

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento(request.tipoMovimiento());
        movimiento.setCantidad(request.cantidad());
        movimiento.setStockAntes(stockAntes);
        movimiento.setStockDespues(stockDespues);
        movimiento.setMotivo(request.motivo());
        movimiento.setObservacion(request.observacion());
        movimiento.setUsuarioResponsable(request.usuarioResponsable());
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setInventario(inventario);
        movimiento.setLoteMedicamento(lote);

        MovimientoInventario movimientoGuardado = movimientoInventarioRepository.save(movimiento);

        alertaStockService.evaluarInventario(inventario);

        return toMovimientoResponse(movimientoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoInventarioResponse consultarPorId(Long id) {
        MovimientoInventario movimiento = movimientoInventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento de inventario no encontrado con ID: " + id));

        return toMovimientoResponse(movimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoInventarioResponse> consultarKardexPorInventario(Long inventarioId) {
        if (!inventarioRepository.existsById(inventarioId)) {
            throw new ResourceNotFoundException("Inventario no encontrado con ID: " + inventarioId);
        }

        return movimientoInventarioRepository.findByInventarioIdOrderByFechaMovimientoDesc(inventarioId)
                .stream()
                .map(this::toMovimientoResponse)
                .toList();
    }

    private void validarLotePerteneceAlMedicamentoDelInventario(Inventario inventario, LoteMedicamento lote) {
        Long medicamentoInventarioId = inventario.getMedicamento().getId();
        Long medicamentoLoteId = lote.getMedicamento().getId();

        if (!medicamentoInventarioId.equals(medicamentoLoteId)) {
            throw new BusinessRuleException("El lote no pertenece al medicamento asociado al inventario");
        }
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new BusinessRuleException("La cantidad debe ser mayor a cero");
        }
    }

    private int calcularStockDespues(int stockAntes, int cantidad, TipoMovimiento tipoMovimiento) {
        return switch (tipoMovimiento) {
            case ENTRADA, AJUSTE_POSITIVO -> stockAntes + cantidad;
            case SALIDA, AJUSTE_NEGATIVO -> stockAntes - cantidad;
        };
    }

    private void validarMovimiento(
            TipoMovimiento tipoMovimiento,
            int cantidad,
            Inventario inventario,
            LoteMedicamento lote
    ) {
        if (lote.getEstado() == EstadoLote.BLOQUEADO) {
            throw new BusinessRuleException("No se pueden realizar movimientos sobre un lote bloqueado");
        }

        if (lote.getFechaVencimiento().isBefore(LocalDate.now())) {
            lote.setEstado(EstadoLote.VENCIDO);
            throw new BusinessRuleException("No se pueden realizar movimientos sobre un lote vencido");
        }

        if (tipoMovimiento == TipoMovimiento.SALIDA || tipoMovimiento == TipoMovimiento.AJUSTE_NEGATIVO) {
            if (inventario.getStockActual() < cantidad) {
                throw new BusinessRuleException("Stock insuficiente en inventario");
            }

            if (lote.getCantidadActual() < cantidad) {
                throw new BusinessRuleException("Cantidad insuficiente en el lote");
            }
        }
    }

    private void actualizarInventario(Inventario inventario, int stockDespues) {
        if (stockDespues < 0) {
            throw new BusinessRuleException("El stock no puede quedar negativo");
        }

        inventario.setStockActual(stockDespues);
        inventario.setStockDisponible(stockDespues - inventario.getStockReservado());
        inventario.setFechaUltimaActualizacion(LocalDateTime.now());

        inventarioRepository.save(inventario);
    }

    private void actualizarLote(LoteMedicamento lote, TipoMovimiento tipoMovimiento, int cantidad) {
        int cantidadActual = lote.getCantidadActual();

        switch (tipoMovimiento) {
            case ENTRADA, AJUSTE_POSITIVO -> cantidadActual += cantidad;
            case SALIDA, AJUSTE_NEGATIVO -> cantidadActual -= cantidad;
        }

        if (cantidadActual < 0) {
            throw new BusinessRuleException("La cantidad del lote no puede quedar negativa");
        }

        lote.setCantidadActual(cantidadActual);

        if (cantidadActual == 0) {
            lote.setEstado(EstadoLote.AGOTADO);
        } else if (lote.getFechaVencimiento().isBefore(LocalDate.now())) {
            lote.setEstado(EstadoLote.VENCIDO);
        } else if (lote.getEstado() == EstadoLote.AGOTADO) {
            lote.setEstado(EstadoLote.ACTIVO);
        }

        loteMedicamentoRepository.save(lote);
    }

    private MovimientoInventarioResponse toMovimientoResponse(MovimientoInventario movimiento) {
        Inventario inventario = movimiento.getInventario();
        LoteMedicamento lote = movimiento.getLoteMedicamento();

        return new MovimientoInventarioResponse(
                movimiento.getId(),
                movimiento.getTipoMovimiento(),
                movimiento.getCantidad(),
                movimiento.getStockAntes(),
                movimiento.getStockDespues(),
                movimiento.getMotivo(),
                movimiento.getObservacion(),
                movimiento.getUsuarioResponsable(),
                movimiento.getFechaMovimiento(),

                inventario.getId(),
                inventario.getMedicamento().getId(),
                inventario.getMedicamento().getCodigo(),
                inventario.getMedicamento().getNombre(),

                lote.getId(),
                lote.getNumeroLote(),

                inventario.getCentroDistribucion().getId(),
                inventario.getCentroDistribucion().getNombre()
        );
    }
}
