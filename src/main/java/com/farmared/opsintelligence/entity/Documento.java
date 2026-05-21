package com.farmared.opsintelligence.entity;

import com.farmared.opsintelligence.entity.enums.EstadoDocumento;
import com.farmared.opsintelligence.entity.enums.TipoDocumento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "documentos")
public class Documento extends BaseEntity {

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "nombre_almacenado", nullable = false, unique = true, length = 255)
    private String nombreAlmacenado;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 50)
    private TipoDocumento tipoDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_documento", nullable = false, length = 30)
    private EstadoDocumento estadoDocumento = EstadoDocumento.ACTIVO;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "modulo_referencia", length = 100)
    private String moduloReferencia;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "usuario_carga", nullable = false, length = 150)
    private String usuarioCarga;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;

    @Column(nullable = false)
    private Boolean activo = true;
}
