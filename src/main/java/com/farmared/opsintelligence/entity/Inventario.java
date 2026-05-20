package com.farmared.opsintelligence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "inventarios",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventario_medicamento_centro",
                        columnNames = {"medicamento_id", "centro_distribucion_id"}
                )
        }
)
public class Inventario extends BaseEntity {

    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    @Column(name = "stock_reservado", nullable = false)
    private Integer stockReservado = 0;

    @Column(name = "stock_disponible", nullable = false)
    private Integer stockDisponible = 0;

    @Column(name = "fecha_ultima_actualizacion")
    private LocalDateTime fechaUltimaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_distribucion_id", nullable = false)
    private CentroDistribucion centroDistribucion;

    @OneToMany(mappedBy = "inventario")
    private List<MovimientoInventario> movimientos = new ArrayList<>();
}