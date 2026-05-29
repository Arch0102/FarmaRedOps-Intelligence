package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.MedicamentoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicamentoProveedorRepository extends JpaRepository<MedicamentoProveedor, Long> {

    Optional<MedicamentoProveedor> findByMedicamentoIdAndProveedorId(Long medicamentoId, Long proveedorId);

    boolean existsByMedicamentoIdAndProveedorId(Long medicamentoId, Long proveedorId);

    List<MedicamentoProveedor> findByMedicamentoId(Long medicamentoId);

    List<MedicamentoProveedor> findByProveedorId(Long proveedorId);

    List<MedicamentoProveedor> findByActivoTrue();
}