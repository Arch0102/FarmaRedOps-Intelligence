package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.response.DashboardMetricaResponse;
import com.farmared.opsintelligence.dto.response.DashboardResumenResponse;
import com.farmared.opsintelligence.entity.DashboardMetrica;
import com.farmared.opsintelligence.entity.Inventario;
import com.farmared.opsintelligence.entity.LoteMedicamento;
import com.farmared.opsintelligence.entity.OrdenCompra;
import com.farmared.opsintelligence.entity.enums.EstadoAlerta;
import com.farmared.opsintelligence.entity.enums.EstadoOrdenCompra;
import com.farmared.opsintelligence.entity.enums.TipoMetricaDashboard;
import com.farmared.opsintelligence.repository.AlertaStockRepository;
import com.farmared.opsintelligence.repository.DashboardMetricaRepository;
import com.farmared.opsintelligence.repository.InventarioRepository;
import com.farmared.opsintelligence.repository.LoteMedicamentoRepository;
import com.farmared.opsintelligence.repository.OrdenCompraRepository;
import com.farmared.opsintelligence.service.DashboardMetricaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DashboardMetricaServiceImpl implements DashboardMetricaService {

    private static final int DIAS_PROXIMO_VENCIMIENTO = 30;

    private final DashboardMetricaRepository dashboardMetricaRepository;
    private final InventarioRepository inventarioRepository;
    private final LoteMedicamentoRepository loteMedicamentoRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final AlertaStockRepository alertaStockRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResumenResponse obtenerResumenGeneral() {
        List<Inventario> inventarios = inventarioRepository.findAll();

        long totalStockCritico = inventarios.stream()
                .filter(inventario -> inventario.getStockActual() <= inventario.getMedicamento().getPuntoReorden())
                .count();

        long totalLotesProximosVencer = loteMedicamentoRepository
                .findByFechaVencimientoBefore(LocalDate.now().plusDays(DIAS_PROXIMO_VENCIMIENTO))
                .size();

        List<OrdenCompra> ordenesPendientes = ordenCompraRepository.findByEstado(EstadoOrdenCompra.PENDIENTE);

        BigDecimal valorTotalOrdenesPendientes = ordenesPendientes.stream()
                .map(OrdenCompra::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalAlertasPendientes = alertaStockRepository.findByEstadoAlerta(EstadoAlerta.PENDIENTE).size();

        return new DashboardResumenResponse(
                (long) inventarios.size(),
                totalStockCritico,
                totalLotesProximosVencer,
                (long) ordenesPendientes.size(),
                totalAlertasPendientes,
                valorTotalOrdenesPendientes
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardMetricaResponse> listarMetricas() {
        return dashboardMetricaRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardMetricaResponse> listarMetricasPorTipo(TipoMetricaDashboard tipoMetrica) {
        return dashboardMetricaRepository.findByTipoMetrica(tipoMetrica)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<DashboardMetricaResponse> recalcularMetricas() {
        DashboardResumenResponse resumen = obtenerResumenGeneral();

        guardarMetrica(
                TipoMetricaDashboard.STOCK_CRITICO,
                BigDecimal.valueOf(resumen.totalStockCritico()),
                "inventarios",
                "Cantidad de inventarios con stock igual o inferior al punto de reorden"
        );

        guardarMetrica(
                TipoMetricaDashboard.PROXIMO_VENCIMIENTO,
                BigDecimal.valueOf(resumen.totalLotesProximosVencer()),
                "lotes",
                "Cantidad de lotes próximos a vencer en los próximos " + DIAS_PROXIMO_VENCIMIENTO + " días"
        );

        guardarMetrica(
                TipoMetricaDashboard.ORDENES_PENDIENTES,
                BigDecimal.valueOf(resumen.totalOrdenesPendientes()),
                "órdenes",
                "Cantidad de órdenes de compra pendientes"
        );

        return listarMetricas();
    }

    private void guardarMetrica(
            TipoMetricaDashboard tipoMetrica,
            BigDecimal valor,
            String unidad,
            String descripcion
    ) {
        DashboardMetrica metrica = new DashboardMetrica();
        metrica.setTipoMetrica(tipoMetrica);
        metrica.setValor(valor);
        metrica.setUnidad(unidad);
        metrica.setDescripcion(descripcion);
        metrica.setFechaCalculo(LocalDateTime.now());

        dashboardMetricaRepository.save(metrica);
    }

    private DashboardMetricaResponse toResponse(DashboardMetrica metrica) {
        Long medicamentoId = metrica.getMedicamento() != null ? metrica.getMedicamento().getId() : null;
        String medicamentoNombre = metrica.getMedicamento() != null ? metrica.getMedicamento().getNombre() : null;

        Long centroId = metrica.getCentroDistribucion() != null ? metrica.getCentroDistribucion().getId() : null;
        String centroNombre = metrica.getCentroDistribucion() != null ? metrica.getCentroDistribucion().getNombre() : null;

        return new DashboardMetricaResponse(
                metrica.getId(),
                metrica.getTipoMetrica(),
                metrica.getValor(),
                metrica.getUnidad(),
                metrica.getDescripcion(),
                metrica.getFechaCalculo(),
                medicamentoId,
                medicamentoNombre,
                centroId,
                centroNombre
        );
    }
}