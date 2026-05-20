package com.farmared.opsintelligence.service;

import com.farmared.opsintelligence.dto.response.DashboardMetricaResponse;
import com.farmared.opsintelligence.dto.response.DashboardResumenResponse;
import com.farmared.opsintelligence.entity.enums.TipoMetricaDashboard;

import java.util.List;

public interface DashboardMetricaService {

    DashboardResumenResponse obtenerResumenGeneral();

    List<DashboardMetricaResponse> listarMetricas();

    List<DashboardMetricaResponse> listarMetricasPorTipo(TipoMetricaDashboard tipoMetrica);

    List<DashboardMetricaResponse> recalcularMetricas();
}