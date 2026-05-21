package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.request.DocumentoUpdateRequest;
import com.farmared.opsintelligence.dto.request.DocumentoUploadRequest;
import com.farmared.opsintelligence.dto.response.DocumentoResponse;
import com.farmared.opsintelligence.entity.Documento;
import com.farmared.opsintelligence.entity.enums.EstadoDocumento;
import com.farmared.opsintelligence.entity.enums.TipoDocumento;
import com.farmared.opsintelligence.exception.BadRequestException;
import com.farmared.opsintelligence.exception.ResourceNotFoundException;
import com.farmared.opsintelligence.repository.DocumentoRepository;
import com.farmared.opsintelligence.service.DocumentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentoServiceImpl implements DocumentoService {

    private final DocumentoRepository documentoRepository;

    @Value("${app.documents.storage-path}")
    private String storagePath;

    @Value("${app.documents.max-size-bytes}")
    private Long maxSizeBytes;

    @Override
    public DocumentoResponse subirDocumento(MultipartFile archivo, DocumentoUploadRequest request) {
        validarArchivo(archivo);

        Path directorio = obtenerDirectorioAlmacenamiento();
        crearDirectorioSiNoExiste(directorio);

        String nombreOriginal = StringUtils.cleanPath(archivo.getOriginalFilename());
        String extension = obtenerExtension(nombreOriginal);
        String nombreAlmacenado = UUID.randomUUID() + extension;

        Path rutaDestino = directorio.resolve(nombreAlmacenado).normalize();

        try {
            archivo.transferTo(rutaDestino);
        } catch (IOException exception) {
            throw new BadRequestException("No fue posible almacenar el documento");
        }

        Documento documento = new Documento();
        documento.setNombreOriginal(nombreOriginal);
        documento.setNombreAlmacenado(nombreAlmacenado);
        documento.setContentType(archivo.getContentType());
        documento.setTamanoBytes(archivo.getSize());
        documento.setRutaArchivo(rutaDestino.toString());
        documento.setTipoDocumento(request.tipoDocumento());
        documento.setEstadoDocumento(EstadoDocumento.ACTIVO);
        documento.setDescripcion(request.descripcion());
        documento.setModuloReferencia(request.moduloReferencia());
        documento.setReferenciaId(request.referenciaId());
        documento.setUsuarioCarga(request.usuarioCarga());
        documento.setFechaCarga(LocalDateTime.now());
        documento.setActivo(true);

        Documento documentoGuardado = documentoRepository.save(documento);

        return toResponse(documentoGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoResponse> listarDocumentosActivos() {
        return documentoRepository.findByActivoTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoResponse consultarPorId(Long id) {
        Documento documento = buscarDocumentoActivoPorId(id);
        return toResponse(documento);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource descargarDocumento(Long id) {
        Documento documento = buscarDocumentoActivoPorId(id);

        try {
            Path rutaArchivo = Paths.get(documento.getRutaArchivo()).normalize();
            Resource resource = new UrlResource(rutaArchivo.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("El archivo físico no existe o no se puede leer");
            }

            return resource;
        } catch (MalformedURLException exception) {
            throw new BadRequestException("La ruta del archivo no es válida");
        }
    }

    @Override
    public DocumentoResponse actualizarDocumento(Long id, DocumentoUpdateRequest request) {
        Documento documento = buscarDocumentoActivoPorId(id);

        if (request.tipoDocumento() != null) {
            documento.setTipoDocumento(request.tipoDocumento());
        }

        if (request.estadoDocumento() != null) {
            documento.setEstadoDocumento(request.estadoDocumento());
            documento.setActivo(request.estadoDocumento() == EstadoDocumento.ACTIVO);
        }

        documento.setDescripcion(request.descripcion());
        documento.setModuloReferencia(request.moduloReferencia());
        documento.setReferenciaId(request.referenciaId());

        Documento documentoActualizado = documentoRepository.save(documento);

        return toResponse(documentoActualizado);
    }

    @Override
    public void eliminarDocumento(Long id) {
        Documento documento = buscarDocumentoActivoPorId(id);

        documento.setActivo(false);
        documento.setEstadoDocumento(EstadoDocumento.ELIMINADO);

        documentoRepository.save(documento);
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("El archivo es obligatorio");
        }

        if (archivo.getSize() > maxSizeBytes) {
            throw new BadRequestException("El archivo supera el tamaño máximo permitido");
        }

        String contentType = archivo.getContentType();

        if (!"application/pdf".equalsIgnoreCase(contentType)) {
            throw new BadRequestException("Solo se permiten archivos PDF");
        }

        String nombreOriginal = archivo.getOriginalFilename();

        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            throw new BadRequestException("El nombre del archivo no es válido");
        }

        String nombreLimpio = StringUtils.cleanPath(nombreOriginal);

        if (!nombreLimpio.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("El archivo debe tener extensión .pdf");
        }

        if (nombreLimpio.contains("..")) {
            throw new BadRequestException("El nombre del archivo contiene una ruta inválida");
        }
    }

    private Path obtenerDirectorioAlmacenamiento() {
        return Paths.get(storagePath).toAbsolutePath().normalize();
    }

    private void crearDirectorioSiNoExiste(Path directorio) {
        try {
            Files.createDirectories(directorio);
        } catch (IOException exception) {
            throw new BadRequestException("No fue posible crear el directorio de documentos");
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        int index = nombreArchivo.lastIndexOf(".");

        if (index == -1) {
            return "";
        }

        return nombreArchivo.substring(index);
    }

    private Documento buscarDocumentoActivoPorId(Long id) {
        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con ID: " + id));

        if (!Boolean.TRUE.equals(documento.getActivo())) {
            throw new ResourceNotFoundException("Documento no encontrado con ID: " + id);
        }

        return documento;
    }

    private DocumentoResponse toResponse(Documento documento) {
        return new DocumentoResponse(
                documento.getId(),
                documento.getNombreOriginal(),
                documento.getNombreAlmacenado(),
                documento.getContentType(),
                documento.getTamanoBytes(),
                documento.getRutaArchivo(),
                documento.getTipoDocumento(),
                documento.getEstadoDocumento(),
                documento.getDescripcion(),
                documento.getModuloReferencia(),
                documento.getReferenciaId(),
                documento.getUsuarioCarga(),
                documento.getFechaCarga(),
                documento.getActivo()
        );
    }
}