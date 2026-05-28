package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.response.ApiResponse;
import com.farmared.opsintelligence.dto.response.DashboardMetricaResponse;
import com.farmared.opsintelligence.dto.response.DashboardResumenResponse;
import com.farmared.opsintelligence.entity.enums.TipoMetricaDashboard;
import com.farmared.opsintelligence.service.DashboardMetricaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardMetricaController {

    private final DashboardMetricaService dashboardMetricaService;

    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<DashboardResumenResponse>> obtenerResumenGeneral(HttpServletRequest request) {
        DashboardResumenResponse response = dashboardMetricaService.obtenerResumenGeneral();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Resumen de dashboard consultado correctamente", request.getRequestURI(), response));
    }

    @GetMapping("/metricas")
    public ResponseEntity<ApiResponse<List<DashboardMetricaResponse>>> listarMetricas(HttpServletRequest request) {
        List<DashboardMetricaResponse> response = dashboardMetricaService.listarMetricas();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Metricas de dashboard consultadas correctamente", request.getRequestURI(), response));
    }

    @GetMapping("/metricas/tipo/{tipoMetrica}")
    public ResponseEntity<ApiResponse<List<DashboardMetricaResponse>>> listarMetricasPorTipo(
            @PathVariable TipoMetricaDashboard tipoMetrica,
            HttpServletRequest request
    ) {
        List<DashboardMetricaResponse> response =
                dashboardMetricaService.listarMetricasPorTipo(tipoMetrica);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Metricas de dashboard consultadas por tipo correctamente", request.getRequestURI(), response));
    }

    @PostMapping({"/metricas/recalcular", "/recalcular"})
    public ResponseEntity<ApiResponse<List<DashboardMetricaResponse>>> recalcularMetricas(HttpServletRequest request) {
        List<DashboardMetricaResponse> response = dashboardMetricaService.recalcularMetricas();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Metricas recalculadas correctamente", request.getRequestURI(), response));
    }
}
