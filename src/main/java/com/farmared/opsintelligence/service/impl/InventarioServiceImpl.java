package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.response.EstadoStock;
import com.farmared.opsintelligence.dto.response.InventarioResponse;
import com.farmared.opsintelligence.dto.response.InventarioResumenResponse;
import com.farmared.opsintelligence.entity.CentroDistribucion;
import com.farmared.opsintelligence.entity.Inventario;
import com.farmared.opsintelligence.entity.Medicamento;
import com.farmared.opsintelligence.exception.ResourceNotFoundException;
import com.farmared.opsintelligence.repository.InventarioRepository;
import com.farmared.opsintelligence.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;

    @Override
    public List<InventarioResponse> listar() {
        return inventarioRepository.findAllWithDetails()
                .stream()
                .sorted(Comparator.comparing(this::medicamentoNombreSeguro))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public InventarioResponse consultarPorId(Long id) {
        Inventario inventario = inventarioRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario no encontrado con ID: " + id));

        return toResponse(inventario);
    }

    @Override
    public InventarioResumenResponse obtenerResumen() {
        List<Inventario> inventarios = inventarioRepository.findAllWithDetails();

        long stockTotal = inventarios.stream().mapToLong(item -> nullSafe(item.getStockActual())).sum();
        long stockDisponibleTotal = inventarios.stream().mapToLong(item -> nullSafe(item.getStockDisponible())).sum();
        long stockReservadoTotal = inventarios.stream().mapToLong(item -> nullSafe(item.getStockReservado())).sum();
        long inventariosEnQuiebre = inventarios.stream()
                .filter(item -> nullSafe(item.getStockActual()) <= 0)
                .count();
        long inventariosCriticos = inventarios.stream()
                .filter(item -> calcularEstadoStock(item) == EstadoStock.CRITICO)
                .count();
        long riesgosStock = inventarios.stream()
                .filter(item -> calcularEstadoStock(item) != EstadoStock.NORMAL)
                .count();

        return new InventarioResumenResponse(
                (long) inventarios.size(),
                stockTotal,
                stockDisponibleTotal,
                riesgosStock,
                stockDisponibleTotal,
                stockReservadoTotal,
                inventariosEnQuiebre,
                inventariosCriticos,
                agruparStockPorMedicamento(inventarios),
                agruparStockPorCentro(inventarios),
                agruparStockPorCategoria(inventarios)
        );
    }

    private InventarioResponse toResponse(Inventario inventario) {
        Medicamento medicamento = inventario.getMedicamento();
        CentroDistribucion centro = inventario.getCentroDistribucion();

        return new InventarioResponse(
                inventario.getId(),
                medicamento != null ? medicamento.getId() : null,
                medicamento != null ? medicamento.getCodigo() : null,
                medicamento != null ? medicamento.getNombre() : "Sin medicamento",
                centro != null ? centro.getId() : null,
                centro != null ? centro.getNombre() : "Sin centro",
                nullSafe(inventario.getStockActual()),
                nullSafe(inventario.getStockReservado()),
                nullSafe(inventario.getStockDisponible()),
                medicamento != null ? nullSafe(medicamento.getStockMinimo()) : 0,
                medicamento != null ? nullSafe(medicamento.getPuntoReorden()) : 0,
                calcularEstadoStock(inventario),
                inventario.getFechaUltimaActualizacion()
        );
    }

    private EstadoStock calcularEstadoStock(Inventario inventario) {
        int stockActual = nullSafe(inventario.getStockActual());
        Medicamento medicamento = inventario.getMedicamento();
        int stockMinimo = medicamento != null ? nullSafe(medicamento.getStockMinimo()) : 0;
        int puntoReorden = medicamento != null ? nullSafe(medicamento.getPuntoReorden()) : 0;

        if (stockActual <= 0) {
            return EstadoStock.QUIEBRE;
        }
        if (stockActual <= stockMinimo) {
            return EstadoStock.CRITICO;
        }
        if (stockActual <= puntoReorden) {
            return EstadoStock.PUNTO_REORDEN;
        }
        return EstadoStock.NORMAL;
    }

    private Map<String, Long> agruparStockPorMedicamento(List<Inventario> inventarios) {
        return ordenarMapa(inventarios.stream().collect(Collectors.groupingBy(
                this::medicamentoNombreSeguro,
                Collectors.summingLong(item -> nullSafe(item.getStockActual()))
        )));
    }

    private Map<String, Long> agruparStockPorCentro(List<Inventario> inventarios) {
        return ordenarMapa(inventarios.stream().collect(Collectors.groupingBy(
                item -> item.getCentroDistribucion() != null ? item.getCentroDistribucion().getNombre() : "Sin centro",
                Collectors.summingLong(item -> nullSafe(item.getStockActual()))
        )));
    }

    private Map<String, Long> agruparStockPorCategoria(List<Inventario> inventarios) {
        return ordenarMapa(inventarios.stream().collect(Collectors.groupingBy(
                item -> {
                    Medicamento medicamento = item.getMedicamento();
                    if (medicamento == null || medicamento.getCategoriaMedicamento() == null) {
                        return "Sin categoria";
                    }
                    return medicamento.getCategoriaMedicamento().getNombre();
                },
                Collectors.summingLong(item -> nullSafe(item.getStockActual()))
        )));
    }

    private Map<String, Long> ordenarMapa(Map<String, Long> source) {
        return source.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private String medicamentoNombreSeguro(Inventario inventario) {
        Medicamento medicamento = inventario.getMedicamento();
        return medicamento != null && medicamento.getNombre() != null ? medicamento.getNombre() : "Sin medicamento";
    }

    private int nullSafe(Integer value) {
        return value != null ? value : 0;
    }
}
