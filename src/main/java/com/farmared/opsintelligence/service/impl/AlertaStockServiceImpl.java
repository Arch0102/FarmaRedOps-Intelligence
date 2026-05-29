package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.response.AlertaStockResponse;
import com.farmared.opsintelligence.entity.AlertaStock;
import com.farmared.opsintelligence.entity.CentroDistribucion;
import com.farmared.opsintelligence.entity.Inventario;
import com.farmared.opsintelligence.entity.LoteMedicamento;
import com.farmared.opsintelligence.entity.Medicamento;
import com.farmared.opsintelligence.entity.enums.EstadoAlerta;
import com.farmared.opsintelligence.entity.enums.TipoAlerta;
import com.farmared.opsintelligence.exception.ResourceNotFoundException;
import com.farmared.opsintelligence.repository.AlertaStockRepository;
import com.farmared.opsintelligence.repository.InventarioRepository;
import com.farmared.opsintelligence.service.AlertaStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AlertaStockServiceImpl implements AlertaStockService {

    private final AlertaStockRepository alertaStockRepository;
    private final InventarioRepository inventarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AlertaStockResponse> listarPendientes() {
        return alertaStockRepository.findByEstadoAlertaWithDetails(EstadoAlerta.PENDIENTE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AlertaStockResponse resolver(Long id) {
        AlertaStock alerta = alertaStockRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta de stock no encontrada con ID: " + id));

        alerta.setEstadoAlerta(EstadoAlerta.RESUELTA);
        alerta.setFechaResolucion(LocalDateTime.now());

        return toResponse(alertaStockRepository.save(alerta));
    }

    @Override
    public void evaluarInventario(Inventario inventario) {
        if (inventario == null || inventario.getMedicamento() == null || inventario.getCentroDistribucion() == null) {
            return;
        }

        int stockActual = nullSafe(inventario.getStockActual());
        Medicamento medicamento = inventario.getMedicamento();
        CentroDistribucion centro = inventario.getCentroDistribucion();

        if (stockActual <= 0) {
            crearAlertaSiNoExiste(inventario, TipoAlerta.STOCK_CRITICO,
                    "Quiebre de stock para " + medicamento.getNombre()
                            + " en " + centro.getNombre()
                            + ". Stock actual: " + stockActual + ".");
        }

        if (stockActual <= nullSafe(medicamento.getStockMinimo())) {
            crearAlertaSiNoExiste(inventario, TipoAlerta.STOCK_CRITICO,
                    "Stock critico para " + medicamento.getNombre()
                            + " en " + centro.getNombre()
                            + ". Stock actual: " + stockActual
                            + ", minimo: " + nullSafe(medicamento.getStockMinimo()) + ".");
        }

        if (stockActual <= nullSafe(medicamento.getPuntoReorden())) {
            crearAlertaSiNoExiste(inventario, TipoAlerta.STOCK_CRITICO,
                    "Punto de reorden alcanzado para " + medicamento.getNombre()
                            + " en " + centro.getNombre()
                            + ". Stock actual: " + stockActual
                            + ", punto de reorden: " + nullSafe(medicamento.getPuntoReorden()) + ".");
        }
    }

    @Override
    public void evaluarInventarios() {
        inventarioRepository.findAllWithDetails().forEach(this::evaluarInventario);
    }

    private void crearAlertaSiNoExiste(Inventario inventario, TipoAlerta tipoAlerta, String mensaje) {
        if (inventario.getMedicamento() == null || inventario.getCentroDistribucion() == null) {
            return;
        }

        boolean existePendiente = alertaStockRepository
                .existsByMedicamentoIdAndCentroDistribucionIdAndTipoAlertaAndEstadoAlerta(
                        inventario.getMedicamento().getId(),
                        inventario.getCentroDistribucion().getId(),
                        tipoAlerta,
                        EstadoAlerta.PENDIENTE
                );

        if (existePendiente) {
            return;
        }

        AlertaStock alerta = new AlertaStock();
        alerta.setTipoAlerta(tipoAlerta);
        alerta.setEstadoAlerta(EstadoAlerta.PENDIENTE);
        alerta.setMedicamento(inventario.getMedicamento());
        alerta.setCentroDistribucion(inventario.getCentroDistribucion());
        alerta.setFechaGeneracion(LocalDateTime.now());
        alerta.setMensaje(mensaje);

        alertaStockRepository.save(alerta);
    }

    private AlertaStockResponse toResponse(AlertaStock alerta) {
        Medicamento medicamento = alerta.getMedicamento();
        CentroDistribucion centro = alerta.getCentroDistribucion();
        LoteMedicamento lote = alerta.getLoteMedicamento();

        return new AlertaStockResponse(
                alerta.getId(),
                alerta.getTipoAlerta(),
                alerta.getEstadoAlerta(),
                alerta.getMensaje(),
                medicamento.getId(),
                medicamento.getCodigo(),
                medicamento.getNombre(),
                centro.getId(),
                centro.getNombre(),
                lote != null ? lote.getId() : null,
                lote != null ? lote.getNumeroLote() : null,
                alerta.getFechaGeneracion(),
                alerta.getFechaResolucion()
        );
    }

    private int nullSafe(Integer value) {
        return value != null ? value : 0;
    }
}
