package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.LoteMedicamento;
import com.farmared.opsintelligence.entity.enums.EstadoLote;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoteMedicamentoRepository extends JpaRepository<LoteMedicamento, Long> {

    Optional<LoteMedicamento> findByNumeroLoteAndMedicamentoId(String numeroLote, Long medicamentoId);

    List<LoteMedicamento> findByMedicamentoId(Long medicamentoId);

    List<LoteMedicamento> findByEstado(EstadoLote estado);

    List<LoteMedicamento> findByFechaVencimientoBefore(LocalDate fecha);

    @EntityGraph(attributePaths = {"medicamento"})
    @Query("""
            select l
            from LoteMedicamento l
            order by l.fechaVencimiento asc
            """)
    List<LoteMedicamento> findAllWithDetails();

    @EntityGraph(attributePaths = {"medicamento"})
    @Query("""
            select l
            from LoteMedicamento l
            where l.medicamento.id = :medicamentoId
            order by l.fechaVencimiento asc, l.numeroLote asc
            """)
    List<LoteMedicamento> findByMedicamentoIdWithDetails(@Param("medicamentoId") Long medicamentoId);
}
