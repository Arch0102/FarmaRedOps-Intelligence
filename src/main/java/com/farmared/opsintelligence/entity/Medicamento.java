package com.farmared.opsintelligence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "medicamentos")
public class Medicamento extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "principio_activo", nullable = false, length = 150)
    private String principioActivo;

    @Column(nullable = false, length = 100)
    private String concentracion;

    @Column(nullable = false, length = 100)
    private String presentacion;

    @Column(name = "unidad_medida", nullable = false, length = 50)
    private String unidadMedida;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Column(name = "stock_maximo", nullable = false)
    private Integer stockMaximo;

    @Column(name = "punto_reorden", nullable = false)
    private Integer puntoReorden;

    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_medicamento_id", nullable = false)
    private CategoriaMedicamento categoriaMedicamento;

    @OneToMany(mappedBy = "medicamento")
    private List<Inventario> inventarios = new ArrayList<>();

    @OneToMany(mappedBy = "medicamento")
    private List<LoteMedicamento> lotes = new ArrayList<>();

    @OneToMany(mappedBy = "medicamento")
    private List<AlertaStock> alertasStock = new ArrayList<>();

    @OneToMany(mappedBy = "medicamento")
    private List<MedicamentoProveedor> medicamentosProveedor = new ArrayList<>();

    @OneToMany(mappedBy = "medicamento")
    private List<DetalleOrden> detallesOrden = new ArrayList<>();

    @OneToMany(mappedBy = "medicamento")
    private List<DashboardMetrica> metricasDashboard = new ArrayList<>();
}