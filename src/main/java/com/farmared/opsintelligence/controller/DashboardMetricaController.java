package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.response.DashboardMetricaResponse;
import com.farmared.opsintelligence.dto.response.DashboardResumenResponse;
import com.farmared.opsintelligence.entity.enums.TipoMetricaDashboard;
import com.farmared.opsintelligence.service.DashboardMetricaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardMetricaController {

    private final DashboardMetricaService dashboardMetricaService;

    @GetMapping("/resumen")
    public ResponseEntity<DashboardResumenResponse> obtenerResumenGeneral() {
        DashboardResumenResponse response = dashboardMetricaService.obtenerResumenGeneral();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metricas")
    public ResponseEntity<List<DashboardMetricaResponse>> listarMetricas() {
        List<DashboardMetricaResponse> response = dashboardMetricaService.listarMetricas();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metricas/tipo/{tipoMetrica}")
    public ResponseEntity<List<DashboardMetricaResponse>> listarMetricasPorTipo(
            @PathVariable TipoMetricaDashboard tipoMetrica
    ) {
        List<DashboardMetricaResponse> response =
                dashboardMetricaService.listarMetricasPorTipo(tipoMetrica);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/metricas/recalcular")
    public ResponseEntity<List<DashboardMetricaResponse>> recalcularMetricas() {
        List<DashboardMetricaResponse> response = dashboardMetricaService.recalcularMetricas();
        return ResponseEntity.ok(response);
    }
}