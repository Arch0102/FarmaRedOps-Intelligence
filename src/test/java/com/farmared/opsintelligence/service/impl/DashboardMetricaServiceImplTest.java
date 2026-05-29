package com.farmared.opsintelligence.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.farmared.opsintelligence.dto.response.DashboardMetricaResponse;
import com.farmared.opsintelligence.dto.response.DashboardResumenResponse;
import com.farmared.opsintelligence.entity.AlertaStock;
import com.farmared.opsintelligence.entity.CategoriaMedicamento;
import com.farmared.opsintelligence.entity.CentroDistribucion;
import com.farmared.opsintelligence.entity.DashboardMetrica;
import com.farmared.opsintelligence.entity.Inventario;
import com.farmared.opsintelligence.entity.Medicamento;
import com.farmared.opsintelligence.entity.enums.EstadoAlerta;
import com.farmared.opsintelligence.entity.enums.EstadoOrdenCompra;
import com.farmared.opsintelligence.entity.enums.TipoAlerta;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DashboardMetricaServiceImplTest {

    @Mock
    private DashboardMetricaRepository dashboardMetricaRepository;

    @Mock
    private MedicamentoRepository medicamentoRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private LoteMedicamentoRepository loteMedicamentoRepository;

    @Mock
    private OrdenCompraRepository ordenCompraRepository;

    @Mock
    private AlertaStockRepository alertaStockRepository;

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Mock
    private AlertaStockService alertaStockService;

    @InjectMocks
    private DashboardMetricaServiceImpl dashboardMetricaService;

    @Test
    void recalcularMetricasNoFallaConListasVacias() {
        // Arrange
        stubResumenDependencies(Collections.emptyList(), Collections.emptyList());
        when(dashboardMetricaRepository.save(any(DashboardMetrica.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dashboardMetricaRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<DashboardMetricaResponse> response = dashboardMetricaService.recalcularMetricas();

        // Assert
        ArgumentCaptor<DashboardMetrica> metricaCaptor = ArgumentCaptor.forClass(DashboardMetrica.class);
        verify(alertaStockService).evaluarInventarios();
        verify(dashboardMetricaRepository, times(3)).save(metricaCaptor.capture());

        List<TipoMetricaDashboard> tipos = metricaCaptor.getAllValues()
                .stream()
                .map(DashboardMetrica::getTipoMetrica)
                .toList();

        assertNotNull(response);
        assertEquals(List.of(
                TipoMetricaDashboard.STOCK_CRITICO,
                TipoMetricaDashboard.PROXIMO_VENCIMIENTO,
                TipoMetricaDashboard.ORDENES_PENDIENTES
        ), tipos);
        assertTrue(metricaCaptor.getAllValues().stream().allMatch(metrica -> BigDecimal.ZERO.compareTo(metrica.getValor()) == 0));
    }

    @Test
    void obtenerResumenGeneralCalculaMedicamentosProximosAgotarseConPuntoReorden() {
        // Arrange
        Inventario proximoAgotarse = inventario(1L, "MED-001", "Acetaminofen 500mg", 8, 5, 10);
        Inventario conStockSuficiente = inventario(2L, "MED-002", "Ibuprofeno 400mg", 25, 10, 15);
        stubResumenDependencies(List.of(proximoAgotarse, conStockSuficiente), Collections.emptyList());

        // Act
        DashboardResumenResponse response = dashboardMetricaService.obtenerResumenGeneral();

        // Assert
        assertEquals(1, response.medicamentosProximosAgotarse().size());
        assertEquals("MED-001", response.medicamentosProximosAgotarse().get(0).medicamentoCodigo());
        assertEquals(8, response.medicamentosProximosAgotarse().get(0).stockActual());
    }

    @Test
    void obtenerResumenGeneralNoFallaConTotalesYFilasNulas() {
        // Arrange
        stubResumenDependencies(Collections.emptyList(), Collections.emptyList());
        when(ordenCompraRepository.countByEstadoGrouped()).thenReturn(List.of(
                new Object[]{null, 3L},
                new Object[]{EstadoOrdenCompra.PENDIENTE, null}
        ));
        when(inventarioRepository.sumStockByCategoria()).thenReturn(List.of(
                new Object[]{null, null},
                new Object[]{"Analgesicos", 12L}
        ));
        when(inventarioRepository.sumStockByCentro()).thenReturn(List.of(
                new Object[]{null, null},
                new Object[]{"Centro Principal", 7L}
        ));

        // Act
        DashboardResumenResponse response = dashboardMetricaService.obtenerResumenGeneral();

        // Assert
        assertEquals(BigDecimal.ZERO, response.valorTotalOrdenesPendientes());
        assertEquals(0L, response.stockPorCategoria().get("Sin clasificar"));
        assertEquals(12L, response.stockPorCategoria().get("Analgesicos"));
        assertEquals(0L, response.stockPorCentro().get("Sin clasificar"));
        assertEquals(7L, response.stockPorCentro().get("Centro Principal"));
    }

    @Test
    void obtenerResumenGeneralMapeaAlertasConTipoValidoDelEnum() {
        // Arrange
        AlertaStock alerta = alertaStock(TipoAlerta.STOCK_CRITICO);
        stubResumenDependencies(Collections.emptyList(), List.of(alerta));

        // Act
        DashboardResumenResponse response = dashboardMetricaService.obtenerResumenGeneral();

        // Assert
        TipoAlerta tipoAlerta = response.alertasStock().get(0).tipoAlerta();
        assertEquals(TipoAlerta.STOCK_CRITICO, tipoAlerta);
        assertTrue(Arrays.asList(TipoAlerta.values()).contains(tipoAlerta));
    }

    private void stubResumenDependencies(List<Inventario> inventarios, List<AlertaStock> alertas) {
        when(medicamentoRepository.countByActivoTrue()).thenReturn(0L);
        when(proveedorRepository.countByActivoTrue()).thenReturn(0L);
        when(inventarioRepository.count()).thenReturn((long) inventarios.size());
        when(inventarioRepository.countStockCritico()).thenReturn(0L);
        when(loteMedicamentoRepository.findByFechaVencimientoBefore(any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(ordenCompraRepository.countByEstado(EstadoOrdenCompra.PENDIENTE)).thenReturn(0L);
        when(ordenCompraRepository.sumTotalByEstado(EstadoOrdenCompra.PENDIENTE)).thenReturn(null);
        when(inventarioRepository.findAllWithDetails()).thenReturn(inventarios);
        when(alertaStockRepository.countByEstadoAlerta(EstadoAlerta.PENDIENTE)).thenReturn((long) alertas.size());
        lenient().when(ordenCompraRepository.countByEstadoGrouped()).thenReturn(Collections.emptyList());
        when(movimientoInventarioRepository.findTop10ByOrderByFechaMovimientoDesc()).thenReturn(Collections.emptyList());
        when(inventarioRepository.findInventariosEnQuiebre()).thenReturn(Collections.emptyList());
        when(movimientoInventarioRepository.findRotacionByTipoMovimiento(eq(TipoMovimiento.SALIDA), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        lenient().when(inventarioRepository.sumStockByCategoria()).thenReturn(Collections.emptyList());
        lenient().when(inventarioRepository.sumStockByCentro()).thenReturn(Collections.emptyList());
        when(alertaStockRepository.findByEstadoAlertaWithDetails(EstadoAlerta.PENDIENTE)).thenReturn(alertas);
    }

    private static Inventario inventario(
            Long id,
            String medicamentoCodigo,
            String medicamentoNombre,
            Integer stockActual,
            Integer stockMinimo,
            Integer puntoReorden
    ) {
        CategoriaMedicamento categoria = new CategoriaMedicamento();
        categoria.setId(1L);
        categoria.setNombre("Analgesicos");

        Medicamento medicamento = new Medicamento();
        medicamento.setId(id + 100);
        medicamento.setCodigo(medicamentoCodigo);
        medicamento.setNombre(medicamentoNombre);
        medicamento.setStockMinimo(stockMinimo);
        medicamento.setPuntoReorden(puntoReorden);
        medicamento.setCategoriaMedicamento(categoria);

        CentroDistribucion centro = new CentroDistribucion();
        centro.setId(1L);
        centro.setNombre("Centro Principal");

        Inventario inventario = new Inventario();
        inventario.setId(id);
        inventario.setMedicamento(medicamento);
        inventario.setCentroDistribucion(centro);
        inventario.setStockActual(stockActual);
        inventario.setStockDisponible(stockActual);
        return inventario;
    }

    private static AlertaStock alertaStock(TipoAlerta tipoAlerta) {
        Medicamento medicamento = new Medicamento();
        medicamento.setId(1L);
        medicamento.setCodigo("MED-001");
        medicamento.setNombre("Acetaminofen 500mg");

        CentroDistribucion centro = new CentroDistribucion();
        centro.setId(1L);
        centro.setNombre("Centro Principal");

        AlertaStock alerta = new AlertaStock();
        alerta.setId(1L);
        alerta.setTipoAlerta(tipoAlerta);
        alerta.setEstadoAlerta(EstadoAlerta.PENDIENTE);
        alerta.setMensaje("Stock critico");
        alerta.setMedicamento(medicamento);
        alerta.setCentroDistribucion(centro);
        return alerta;
    }
}
