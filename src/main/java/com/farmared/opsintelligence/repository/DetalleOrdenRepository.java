package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.DetalleOrden;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleOrdenRepository extends JpaRepository<DetalleOrden, Long> {

    List<DetalleOrden> findByOrdenCompraId(Long ordenCompraId);

    List<DetalleOrden> findByMedicamentoId(Long medicamentoId);
}