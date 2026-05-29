package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.DashboardMetrica;
import com.farmared.opsintelligence.entity.enums.TipoMetricaDashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DashboardMetricaRepository extends JpaRepository<DashboardMetrica, Long> {

    List<DashboardMetrica> findByTipoMetrica(TipoMetricaDashboard tipoMetrica);

    List<DashboardMetrica> findByMedicamentoId(Long medicamentoId);

    List<DashboardMetrica> findByCentroDistribucionId(Long centroDistribucionId);

    List<DashboardMetrica> findByFechaCalculoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}