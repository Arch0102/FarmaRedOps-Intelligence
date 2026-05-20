package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.LoteMedicamento;
import com.farmared.opsintelligence.entity.enums.EstadoLote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoteMedicamentoRepository extends JpaRepository<LoteMedicamento, Long> {

    Optional<LoteMedicamento> findByNumeroLoteAndMedicamentoId(String numeroLote, Long medicamentoId);

    List<LoteMedicamento> findByMedicamentoId(Long medicamentoId);

    List<LoteMedicamento> findByEstado(EstadoLote estado);

    List<LoteMedicamento> findByFechaVencimientoBefore(LocalDate fecha);
}
