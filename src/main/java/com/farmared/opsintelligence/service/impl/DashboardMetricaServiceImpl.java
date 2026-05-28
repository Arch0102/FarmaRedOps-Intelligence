package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.response.DashboardMetricaResponse;
import com.farmared.opsintelligence.dto.response.DashboardResumenResponse;
import com.farmared.opsintelligence.dto.response.MovimientoInventarioResponse;
import com.farmared.opsintelligence.entity.DashboardMetrica;
import com.farmared.opsintelligence.entity.Inventario;
import com.farmared.opsintelligence.entity.OrdenCompra;
import com.farmared.opsintelligence.entity.enums.EstadoAlerta;
import com.farmared.opsintelligence.entity.enums.EstadoOrdenCompra;
import com.farmared.opsintelligence.entity.enums.TipoMetricaDashboard;
import com.farmared.opsintelligence.mapper.DashboardMetricaMapper;
import com.farmared.opsintelligence.mapper.MovimientoInventarioMapper;
import com.farmared.opsintelligence.repository.AlertaStockRepository;
import com.farmared.opsintelligence.repository.DashboardMetricaRepository;
import com.farmared.opsintelligence.repository.InventarioRepository;
import com.farmared.opsintelligence.repository.LoteMedicamentoRepository;
import com.farmared.opsintelligence.repository.MedicamentoRepository;
import com.farmared.opsintelligence.repository.MovimientoInventarioRepository;
import com.farmared.opsintelligence.repository.OrdenCompraRepository;
import com.farmared.opsintelligence.repository.ProveedorRepository;
import com.farmared.opsintelligence.service.DashboardMetricaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final MedicamentoRepository medicamentoRepository;
    private final ProveedorRepository proveedorRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final DashboardMetricaMapper dashboardMetricaMapper;
    private final MovimientoInventarioMapper movimientoInventarioMapper;

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

        Map<EstadoOrdenCompra, Long> ordenesPorEstado = Arrays.stream(EstadoOrdenCompra.values())
                .collect(Collectors.toMap(
                        estado -> estado,
                        ordenCompraRepository::countByEstado,
                        (estadoActual, estadoNuevo) -> estadoActual,
                        LinkedHashMap::new
                ));

        List<OrdenCompra> ordenesPendientes = ordenCompraRepository.findByEstado(EstadoOrdenCompra.PENDIENTE);

        BigDecimal valorTotalOrdenesPendientes = ordenesPendientes.stream()
                .map(OrdenCompra::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalAlertasPendientes = alertaStockRepository.findByEstadoAlerta(EstadoAlerta.PENDIENTE).size();

        int cantidadTotalInventario = inventarios.stream()
                .mapToInt(inventario -> inventario.getStockActual() != null ? inventario.getStockActual() : 0)
                .sum();

        List<MovimientoInventarioResponse> movimientosRecientes = movimientoInventarioRepository
                .findTop10ByOrderByFechaMovimientoDesc()
                .stream()
                .map(movimientoInventarioMapper::toResponse)
                .toList();

        return new DashboardResumenResponse(
                medicamentoRepository.countByActivoTrue(),
                proveedorRepository.countByActivoTrue(),
                ordenCompraRepository.count(),
                ordenesPorEstado,
                (long) inventarios.size(),
                totalStockCritico,
                totalLotesProximosVencer,
                ordenesPorEstado.getOrDefault(EstadoOrdenCompra.PENDIENTE, 0L),
                totalAlertasPendientes,
                valorTotalOrdenesPendientes,
                cantidadTotalInventario,
                movimientosRecientes
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardMetricaResponse> listarMetricas() {
        return dashboardMetricaRepository.findAll()
                .stream()
                .map(dashboardMetricaMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardMetricaResponse> listarMetricasPorTipo(TipoMetricaDashboard tipoMetrica) {
        return dashboardMetricaRepository.findByTipoMetrica(tipoMetrica)
                .stream()
                .map(dashboardMetricaMapper::toResponse)
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
                "Cantidad de lotes proximos a vencer en los proximos " + DIAS_PROXIMO_VENCIMIENTO + " dias"
        );

        guardarMetrica(
                TipoMetricaDashboard.ORDENES_PENDIENTES,
                BigDecimal.valueOf(resumen.totalOrdenesPendientes()),
                "ordenes",
                "Cantidad de ordenes de compra pendientes"
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
}
