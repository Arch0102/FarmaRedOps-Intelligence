package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    Optional<Inventario> findByMedicamentoIdAndCentroDistribucionId(
            Long medicamentoId,
            Long centroDistribucionId
    );

    List<Inventario> findByMedicamentoId(Long medicamentoId);

    List<Inventario> findByCentroDistribucionId(Long centroDistribucionId);

    List<Inventario> findByStockActualLessThanEqual(Integer stockActual);
}