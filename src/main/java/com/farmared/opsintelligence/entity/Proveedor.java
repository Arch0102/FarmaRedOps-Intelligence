package com.farmared.opsintelligence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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
@Table(name = "proveedores")
public class Proveedor extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String nit;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 30)
    private String telefono;

    @Column(length = 150)
    private String correo;

    @Column(length = 200)
    private String direccion;

    @Column(nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "proveedor")
    private List<MedicamentoProveedor> medicamentosProveedor = new ArrayList<>();

    @OneToMany(mappedBy = "proveedor")
    private List<OrdenCompra> ordenesCompra = new ArrayList<>();
}