package com.farmared.opsintelligence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "medicamentos_proveedor",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_medicamento_proveedor",
                        columnNames = {"medicamento_id", "proveedor_id"}
                )
        }
)
public class MedicamentoProveedor extends BaseEntity {

    @Column(name = "precio_referencia", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioReferencia;

    @Column(name = "tiempo_entrega_dias", nullable = false)
    private Integer tiempoEntregaDias;

    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;
}