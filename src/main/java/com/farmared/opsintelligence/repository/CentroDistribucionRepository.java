package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.CentroDistribucion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CentroDistribucionRepository extends JpaRepository<CentroDistribucion, Long> {

    Optional<CentroDistribucion> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<CentroDistribucion> findByActivoTrue();
}
