package com.farmared.opsintelligence.entity;

import com.farmared.opsintelligence.entity.enums.EstadoAlerta;
import enums.TipoAlerta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "alertas_stock")
public class AlertaStock extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alerta", nullable = false, length = 40)
    private TipoAlerta tipoAlerta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_alerta", nullable = false, length = 30)
    private EstadoAlerta estadoAlerta = EstadoAlerta.PENDIENTE;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_distribucion_id", nullable = false)
    private CentroDistribucion centroDistribucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_medicamento_id")
    private LoteMedicamento loteMedicamento;
}