package com.farmared.opsintelligence.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.farmared.opsintelligence.entity.AlertaStock;
import com.farmared.opsintelligence.entity.CentroDistribucion;
import com.farmared.opsintelligence.entity.Inventario;
import com.farmared.opsintelligence.entity.Medicamento;
import com.farmared.opsintelligence.entity.enums.EstadoAlerta;
import com.farmared.opsintelligence.entity.enums.TipoAlerta;
import com.farmared.opsintelligence.repository.AlertaStockRepository;
import com.farmared.opsintelligence.repository.InventarioRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlertaStockServiceImplTest {

    @Mock
    private AlertaStockRepository alertaStockRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private AlertaStockServiceImpl alertaStockService;

    @Test
    void evaluarInventarioGeneraAlertaDePuntoReorden() {
        // Arrange
        Inventario inventario = inventario(12, 10, 15);
        when(alertaStockRepository.existsByMedicamentoIdAndCentroDistribucionIdAndTipoAlertaAndEstadoAlerta(
                eq(1L),
                eq(1L),
                eq(TipoAlerta.STOCK_CRITICO),
                eq(EstadoAlerta.PENDIENTE)
        )).thenReturn(false);
        when(alertaStockRepository.save(any(AlertaStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        alertaStockService.evaluarInventario(inventario);

        // Assert
        ArgumentCaptor<AlertaStock> alertaCaptor = ArgumentCaptor.forClass(AlertaStock.class);
        verify(alertaStockRepository).save(alertaCaptor.capture());

        AlertaStock alerta = alertaCaptor.getValue();
        assertEquals(TipoAlerta.STOCK_CRITICO, alerta.getTipoAlerta());
        assertEquals(EstadoAlerta.PENDIENTE, alerta.getEstadoAlerta());
        assertTrue(alerta.getMensaje().contains("Punto de reorden"));
    }

    @Test
    void evaluarInventarioEvitaDuplicarAlertasPendientes() {
        // Arrange
        Inventario inventario = inventario(5, 10, 15);
        when(alertaStockRepository.existsByMedicamentoIdAndCentroDistribucionIdAndTipoAlertaAndEstadoAlerta(
                eq(1L),
                eq(1L),
                eq(TipoAlerta.STOCK_CRITICO),
                eq(EstadoAlerta.PENDIENTE)
        )).thenReturn(true);

        // Act
        alertaStockService.evaluarInventario(inventario);

        // Assert
        verify(alertaStockRepository, never()).save(any(AlertaStock.class));
    }

    @Test
    void evaluarInventariosEvaluaCadaInventarioConDetalles() {
        // Arrange
        Inventario inventario = inventario(12, 10, 15);
        when(inventarioRepository.findAllWithDetails()).thenReturn(List.of(inventario));
        when(alertaStockRepository.existsByMedicamentoIdAndCentroDistribucionIdAndTipoAlertaAndEstadoAlerta(
                eq(1L),
                eq(1L),
                eq(TipoAlerta.STOCK_CRITICO),
                eq(EstadoAlerta.PENDIENTE)
        )).thenReturn(false);
        when(alertaStockRepository.save(any(AlertaStock.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        alertaStockService.evaluarInventarios();

        // Assert
        verify(inventarioRepository).findAllWithDetails();
        verify(alertaStockRepository).save(any(AlertaStock.class));
    }

    @Test
    void evaluarInventarioIgnoraDatosIncompletos() {
        // Arrange
        Inventario inventario = new Inventario();

        // Act
        alertaStockService.evaluarInventario(inventario);

        // Assert
        verify(alertaStockRepository, never()).save(any(AlertaStock.class));
    }

    private static Inventario inventario(Integer stockActual, Integer stockMinimo, Integer puntoReorden) {
        Medicamento medicamento = new Medicamento();
        medicamento.setId(1L);
        medicamento.setCodigo("MED-001");
        medicamento.setNombre("Acetaminofen 500mg");
        medicamento.setStockMinimo(stockMinimo);
        medicamento.setPuntoReorden(puntoReorden);

        CentroDistribucion centro = new CentroDistribucion();
        centro.setId(1L);
        centro.setNombre("Centro Principal");

        Inventario inventario = new Inventario();
        inventario.setId(1L);
        inventario.setStockActual(stockActual);
        inventario.setMedicamento(medicamento);
        inventario.setCentroDistribucion(centro);
        return inventario;
    }
}
