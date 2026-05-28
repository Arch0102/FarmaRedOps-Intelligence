package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.OrdenCompra;
import com.farmared.opsintelligence.entity.enums.EstadoOrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {

    Optional<OrdenCompra> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<OrdenCompra> findByProveedorId(Long proveedorId);

    List<OrdenCompra> findByEstado(EstadoOrdenCompra estado);

    long countByEstado(EstadoOrdenCompra estado);

    @Query("select sum(o.total) from OrdenCompra o where o.estado = :estado")
    BigDecimal sumTotalByEstado(@Param("estado") EstadoOrdenCompra estado);

    @Query("select o.estado, count(o) from OrdenCompra o group by o.estado")
    List<Object[]> countByEstadoGrouped();

    List<OrdenCompra> findByFechaOrdenBetween(LocalDate fechaInicio, LocalDate fechaFin);
}
