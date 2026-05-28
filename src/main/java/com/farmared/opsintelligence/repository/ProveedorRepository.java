package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    Optional<Proveedor> findByNit(String nit);

    boolean existsByNit(String nit);

    long countByActivoTrue();

    List<Proveedor> findByActivoTrue();

    List<Proveedor> findByNombreContainingIgnoreCase(String nombre);
}
