package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.MovimientoInventario;
import com.farmared.opsintelligence.entity.enums.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByInventarioIdOrderByFechaMovimientoDesc(Long inventarioId);

    List<MovimientoInventario> findByLoteMedicamentoIdOrderByFechaMovimientoDesc(Long loteMedicamentoId);

    List<MovimientoInventario> findByTipoMovimiento(TipoMovimiento tipoMovimiento);
}