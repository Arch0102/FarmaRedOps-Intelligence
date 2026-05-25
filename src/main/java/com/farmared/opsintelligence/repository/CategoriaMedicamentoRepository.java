package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.CategoriaMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaMedicamentoRepository extends JpaRepository<CategoriaMedicamento, Long> {

    Optional<CategoriaMedicamento> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    Optional<CategoriaMedicamento> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}
