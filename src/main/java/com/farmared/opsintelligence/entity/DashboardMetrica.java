package com.farmared.opsintelligence.entity;

import com.farmared.opsintelligence.entity.enums.TipoMetricaDashboard;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dashboard_metricas")
public class DashboardMetrica extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_metrica", nullable = false, length = 50)
    private TipoMetricaDashboard tipoMetrica;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 100)
    private String unidad;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "fecha_calculo", nullable = false)
    private LocalDateTime fechaCalculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id")
    private Medicamento medicamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_distribucion_id")
    private CentroDistribucion centroDistribucion;
}