package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.response.DashboardMetricaResponse;
import com.farmared.opsintelligence.dto.response.DashboardResumenResponse;
import com.farmared.opsintelligence.dto.response.AlertaStockResponse;
import com.farmared.opsintelligence.dto.response.MedicamentoRotacionResponse;
import com.farmared.opsintelligence.dto.response.MovimientoInventarioResponse;
import com.farmared.opsintelligence.dto.response.StockMedicamentoResumenResponse;
import com.farmared.opsintelligence.entity.AlertaStock;
import com.farmared.opsintelligence.entity.CentroDistribucion;
import com.farmared.opsintelligence.entity.DashboardMetrica;
import com.farmared.opsintelligence.entity.Inventario;
import com.farmared.opsintelligence.entity.LoteMedicamento;
import com.farmared.opsintelligence.entity.Medicamento;
import com.farmared.opsintelligence.entity.MovimientoInventario;
import com.farmared.opsintelligence.entity.enums.EstadoAlerta;
import com.farmared.opsintelligence.entity.enums.EstadoOrdenCompra;
import com.farmared.opsintelligence.entity.enums.TipoMetricaDashboard;
import com.farmared.opsintelligence.entity.enums.TipoMovimiento;
import com.farmared.opsintelligence.repository.AlertaStockRepository;
import com.farmared.opsintelligence.repository.DashboardMetricaRepository;
import com.farmared.opsintelligence.repository.InventarioRepository;
import com.farmared.opsintelligence.repository.LoteMedicamentoRepository;
import com.farmared.opsintelligence.repository.MedicamentoRepository;
import com.farmared.opsintelligence.repository.MovimientoInventarioRepository;
import com.farmared.opsintelligence.repository.OrdenCompraRepository;
import com.farmared.opsintelligence.repository.ProveedorRepository;
import com.farmared.opsintelligence.service.AlertaStockService;
import com.farmared.opsintelligence.service.DashboardMetricaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class DashboardMetricaServiceImpl implements DashboardMetricaService {

    private static final int DIAS_PROXIMO_VENCIMIENTO = 30;

    private final DashboardMetricaRepository dashboardMetricaRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final ProveedorRepository proveedorRepository;
    private final InventarioRepository inventarioRepository;
    private final LoteMedicamentoRepository loteMedicamentoRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final AlertaStockRepository alertaStockRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final AlertaStockService alertaStockService;

    @Override
    @Transactional(readOnly = true)
    public DashboardResumenResponse obtenerResumenGeneral() {
        long totalMedicamentosActivos = medicamentoRepository.countByActivoTrue();
        long totalProveedoresActivos = proveedorRepository.countByActivoTrue();
        long totalInventarios = inventarioRepository.count();
        long totalStockCritico = inventarioRepository.countStockCritico();

        long totalLotesProximosVencer = loteMedicamentoRepository
                .findByFechaVencimientoBefore(LocalDate.now().plusDays(DIAS_PROXIMO_VENCIMIENTO))
                .size();

        long totalOrdenesPendientes = ordenCompraRepository.countByEstado(EstadoOrdenCompra.PENDIENTE);
        BigDecimal valorTotalOrdenesPendientes =
                ordenCompraRepository.sumTotalByEstado(EstadoOrdenCompra.PENDIENTE);
        if (valorTotalOrdenesPendientes == null) {
            valorTotalOrdenesPendientes = BigDecimal.ZERO;
        }

        long totalAlertasPendientes = alertaStockRepository.countByEstadoAlerta(EstadoAlerta.PENDIENTE);
        Map<String, Long> ordenesPorEstado = obtenerOrdenesPorEstado();
        List<MovimientoInventarioResponse> movimientosRecientes = movimientoInventarioRepository
                .findTop10ByOrderByFechaMovimientoDesc()
                .stream()
                .map(this::toMovimientoResponse)
                .toList();
        List<StockMedicamentoResumenResponse> medicamentosProximosAgotarse = inventarioRepository
                .findInventariosProximosAgotarse()
                .stream()
                .limit(8)
                .map(this::toStockResumenResponse)
                .toList();
        List<StockMedicamentoResumenResponse> medicamentosEnQuiebre = inventarioRepository
                .findInventariosEnQuiebre()
                .stream()
                .limit(8)
                .map(this::toStockResumenResponse)
                .toList();
        List<MedicamentoRotacionResponse> medicamentosMayorRotacion = movimientoInventarioRepository
                .findRotacionByTipoMovimiento(TipoMovimiento.SALIDA, PageRequest.of(0, 6))
                .stream()
                .map(this::toRotacionResponse)
                .toList();
        Map<String, Long> stockPorCategoria = toMap(inventarioRepository.sumStockByCategoria());
        Map<String, Long> stockPorCentro = toMap(inventarioRepository.sumStockByCentro());
        List<AlertaStockResponse> alertasStock = alertaStockRepository
                .findByEstadoAlertaWithDetails(EstadoAlerta.PENDIENTE)
                .stream()
                .limit(8)
                .map(this::toAlertaResponse)
                .toList();

        return new DashboardResumenResponse(
                totalMedicamentosActivos,
                totalProveedoresActivos,
                totalInventarios,
                totalStockCritico,
                totalLotesProximosVencer,
                totalOrdenesPendientes,
                totalAlertasPendientes,
                valorTotalOrdenesPendientes,
                ordenesPorEstado,
                movimientosRecientes,
                medicamentosProximosAgotarse,
                medicamentosEnQuiebre,
                medicamentosMayorRotacion,
                stockPorCategoria,
                stockPorCentro,
                alertasStock
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
        alertaStockService.evaluarInventarios();
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

    private Map<String, Long> obtenerOrdenesPorEstado() {
        Map<EstadoOrdenCompra, Long> conteos = new LinkedHashMap<>();
        for (Object[] row : ordenCompraRepository.countByEstadoGrouped()) {
            EstadoOrdenCompra estado = (EstadoOrdenCompra) row[0];
            long total = ((Number) row[1]).longValue();
            if (total > 0) {
                conteos.put(estado, total);
            }
        }

        Map<String, Long> response = new LinkedHashMap<>();
        for (EstadoOrdenCompra estado : EstadoOrdenCompra.values()) {
            Long total = conteos.get(estado);
            if (total != null) {
                response.put(estado.name(), total);
            }
        }

        return response;
    }

    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> response = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String key = row[0] != null ? String.valueOf(row[0]) : "Sin clasificar";
            long value = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            response.put(key, value);
        }
        return response;
    }

    private StockMedicamentoResumenResponse toStockResumenResponse(Inventario inventario) {
        Medicamento medicamento = inventario.getMedicamento();
        CentroDistribucion centro = inventario.getCentroDistribucion();
        String categoriaNombre = medicamento.getCategoriaMedicamento() != null
                ? medicamento.getCategoriaMedicamento().getNombre()
                : "Sin categoria";

        return new StockMedicamentoResumenResponse(
                inventario.getId(),
                medicamento.getId(),
                medicamento.getCodigo(),
                medicamento.getNombre(),
                categoriaNombre,
                centro.getId(),
                centro.getNombre(),
                inventario.getStockActual(),
                medicamento.getStockMinimo(),
                medicamento.getPuntoReorden()
        );
    }

    private MedicamentoRotacionResponse toRotacionResponse(Object[] row) {
        return new MedicamentoRotacionResponse(
                row[0] != null ? ((Number) row[0]).longValue() : null,
                row[1] != null ? String.valueOf(row[1]) : null,
                row[2] != null ? String.valueOf(row[2]) : null,
                row[3] != null ? ((Number) row[3]).longValue() : 0L
        );
    }

    private AlertaStockResponse toAlertaResponse(AlertaStock alerta) {
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
