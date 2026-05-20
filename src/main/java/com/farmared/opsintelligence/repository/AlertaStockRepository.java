package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.AlertaStock;
import com.farmared.opsintelligence.entity.enums.EstadoAlerta;
import com.farmared.opsintelligence.entity.enums.TipoAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaStockRepository extends JpaRepository<AlertaStock, Long> {

    List<AlertaStock> findByEstadoAlerta(EstadoAlerta estadoAlerta);

    List<AlertaStock> findByMedicamentoIdAndEstadoAlerta(Long medicamentoId, EstadoAlerta estadoAlerta);

    List<AlertaStock> findByCentroDistribucionIdAndEstadoAlerta(Long centroDistribucionId, EstadoAlerta estadoAlerta);

    boolean existsByMedicamentoIdAndCentroDistribucionIdAndTipoAlertaAndEstadoAlerta(
            Long medicamentoId,
            Long centroDistribucionId,
            TipoAlerta tipoAlerta,
            EstadoAlerta estadoAlerta
    );
}
