package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.MovimientoInventario;
import com.farmared.opsintelligence.entity.enums.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByInventarioIdOrderByFechaMovimientoDesc(Long inventarioId);

    List<MovimientoInventario> findByLoteMedicamentoIdOrderByFechaMovimientoDesc(Long loteMedicamentoId);

    List<MovimientoInventario> findByTipoMovimiento(TipoMovimiento tipoMovimiento);

    List<MovimientoInventario> findTop10ByOrderByFechaMovimientoDesc();

    @Query("""
            select med.id, med.codigo, med.nombre, coalesce(sum(m.cantidad), 0)
            from MovimientoInventario m
            join m.inventario i
            join i.medicamento med
            where m.tipoMovimiento = :tipoMovimiento
            group by med.id, med.codigo, med.nombre
            order by sum(m.cantidad) desc
            """)
    List<Object[]> findRotacionByTipoMovimiento(
            @Param("tipoMovimiento") TipoMovimiento tipoMovimiento,
            Pageable pageable
    );
}
