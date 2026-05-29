package com.farmared.opsintelligence.repository;
import com.farmared.opsintelligence.entity.AlertaStock;
import com.farmared.opsintelligence.entity.enums.EstadoAlerta;
import com.farmared.opsintelligence.entity.enums.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlertaStockRepository extends JpaRepository<AlertaStock, Long> {

    List<AlertaStock> findByEstadoAlerta(EstadoAlerta estadoAlerta);

    long countByEstadoAlerta(EstadoAlerta estadoAlerta);

    List<AlertaStock> findByMedicamentoIdAndEstadoAlerta(Long medicamentoId, EstadoAlerta estadoAlerta);

    List<AlertaStock> findByCentroDistribucionIdAndEstadoAlerta(Long centroDistribucionId, EstadoAlerta estadoAlerta);

    boolean existsByMedicamentoIdAndCentroDistribucionIdAndTipoAlertaAndEstadoAlerta(
            Long medicamentoId,
            Long centroDistribucionId,
            TipoAlerta tipoAlerta,
            EstadoAlerta estadoAlerta
    );

    @Query("""
            select a
            from AlertaStock a
            join fetch a.medicamento
            join fetch a.centroDistribucion
            left join fetch a.loteMedicamento
            where a.estadoAlerta = :estadoAlerta
            order by a.fechaGeneracion desc
            """)
    List<AlertaStock> findByEstadoAlertaWithDetails(@Param("estadoAlerta") EstadoAlerta estadoAlerta);

    @Query("""
            select a
            from AlertaStock a
            join fetch a.medicamento
            join fetch a.centroDistribucion
            left join fetch a.loteMedicamento
            where a.id = :id
            """)
    Optional<AlertaStock> findByIdWithDetails(@Param("id") Long id);
}
