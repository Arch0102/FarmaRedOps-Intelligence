package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    Optional<Medicamento> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Medicamento> findByActivoTrue();

    List<Medicamento> findByNombreContainingIgnoreCase(String nombre);
}