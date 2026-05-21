package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.Documento;
import com.farmared.opsintelligence.entity.enums.EstadoDocumento;
import com.farmared.opsintelligence.entity.enums.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    Optional<Documento> findByNombreAlmacenado(String nombreAlmacenado);

    List<Documento> findByEstadoDocumento(EstadoDocumento estadoDocumento);

    List<Documento> findByTipoDocumento(TipoDocumento tipoDocumento);

    List<Documento> findByModuloReferenciaAndReferenciaId(String moduloReferencia, Long referenciaId);

    List<Documento> findByActivoTrue();

    List<Documento> findByUsuarioCargaContainingIgnoreCase(String usuarioCarga);
}