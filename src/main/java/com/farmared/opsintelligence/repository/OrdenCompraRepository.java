package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.OrdenCompra;
import com.farmared.opsintelligence.entity.enums.EstadoOrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {

    Optional<OrdenCompra> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<OrdenCompra> findByProveedorId(Long proveedorId);

    List<OrdenCompra> findByEstado(EstadoOrdenCompra estado);

    long countByEstado(EstadoOrdenCompra estado);

    List<OrdenCompra> findByFechaOrdenBetween(LocalDate fechaInicio, LocalDate fechaFin);
}
