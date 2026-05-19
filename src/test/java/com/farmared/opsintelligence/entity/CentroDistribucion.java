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
@Table(name = "centros_distribucion")
public class CentroDistribucion extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "centroDistribucion")
    private List<Inventario> inventarios = new ArrayList<>();

    @OneToMany(mappedBy = "centroDistribucion")
    private List<AlertaStock> alertasStock = new ArrayList<>();
}