package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.request.DocumentoUpdateRequest;
import com.farmared.opsintelligence.dto.request.DocumentoUploadRequest;
import com.farmared.opsintelligence.dto.response.DashboardResumenResponse;
import com.farmared.opsintelligence.dto.response.DocumentoResponse;
import com.farmared.opsintelligence.dto.response.MovimientoInventarioResponse;
import com.farmared.opsintelligence.entity.Documento;
import com.farmared.opsintelligence.entity.enums.EstadoDocumento;
import com.farmared.opsintelligence.entity.enums.EstadoOrdenCompra;
import com.farmared.opsintelligence.entity.enums.TipoDocumento;
import com.farmared.opsintelligence.exception.BadRequestException;
import com.farmared.opsintelligence.exception.ResourceNotFoundException;
import com.farmared.opsintelligence.mapper.DocumentoMapper;
import com.farmared.opsintelligence.repository.DocumentoRepository;
import com.farmared.opsintelligence.service.DashboardMetricaService;
import com.farmared.opsintelligence.service.DocumentoService;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentoServiceImpl implements DocumentoService {

    private static final String PDF_EXTENSION = ".pdf";
    private static final String MODULO_DASHBOARD = "DASHBOARD";

    private final DocumentoRepository documentoRepository;
    private final DocumentoMapper documentoMapper;
    private final DashboardMetricaService dashboardMetricaService;

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

        return documentoMapper.toResponse(documentoGuardado);
    }

    @Override
    public DocumentoResponse generarReporteDashboard(String usuarioGeneracion) {
        DashboardResumenResponse resumen = dashboardMetricaService.obtenerResumenGeneral();

        Path directorio = obtenerDirectorioAlmacenamiento();
        crearDirectorioSiNoExiste(directorio);

        LocalDateTime fechaGeneracion = LocalDateTime.now();
        String timestamp = fechaGeneracion.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String nombreOriginal = "reporte-dashboard-" + timestamp + PDF_EXTENSION;
        String nombreAlmacenado = UUID.randomUUID() + PDF_EXTENSION;
        Path rutaDestino = directorio.resolve(nombreAlmacenado).normalize();

        escribirReporteDashboard(rutaDestino, resumen, fechaGeneracion);

        Documento documento = new Documento();
        documento.setNombreOriginal(nombreOriginal);
        documento.setNombreAlmacenado(nombreAlmacenado);
        documento.setContentType(MediaType.APPLICATION_PDF_VALUE);
        documento.setTamanoBytes(obtenerTamanoArchivo(rutaDestino));
        documento.setRutaArchivo(rutaDestino.toString());
        documento.setTipoDocumento(TipoDocumento.REPORTE);
        documento.setEstadoDocumento(EstadoDocumento.ACTIVO);
        documento.setDescripcion("Reporte PDF de resumen de dashboard");
        documento.setModuloReferencia(MODULO_DASHBOARD);
        documento.setReferenciaId(null);
        documento.setUsuarioCarga(normalizarUsuario(usuarioGeneracion));
        documento.setFechaCarga(fechaGeneracion);
        documento.setActivo(true);

        return documentoMapper.toResponse(documentoRepository.save(documento));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoResponse> listarDocumentosActivos() {
        return documentoRepository.findByActivoTrue()
                .stream()
                .map(documentoMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoResponse consultarPorId(Long id) {
        Documento documento = buscarDocumentoActivoPorId(id);
        return documentoMapper.toResponse(documento);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource descargarDocumento(Long id) {
        Documento documento = buscarDocumentoActivoPorId(id);

        try {
            Path rutaArchivo = Paths.get(documento.getRutaArchivo()).normalize();
            Resource resource = new UrlResource(rutaArchivo.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("El archivo fisico no existe o no se puede leer");
            }

            return resource;
        } catch (MalformedURLException exception) {
            throw new BadRequestException("La ruta del archivo no es valida");
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

        return documentoMapper.toResponse(documentoActualizado);
    }

    @Override
    public void eliminarDocumento(Long id) {
        Documento documento = buscarDocumentoActivoPorId(id);

        documento.setActivo(false);
        documento.setEstadoDocumento(EstadoDocumento.ELIMINADO);

        documentoRepository.save(documento);
    }

    private void escribirReporteDashboard(
            Path rutaDestino,
            DashboardResumenResponse resumen,
            LocalDateTime fechaGeneracion
    ) {
        try (OutputStream outputStream = Files.newOutputStream(rutaDestino)) {
            com.lowagie.text.Document pdfDocument = new com.lowagie.text.Document();
            PdfWriter.getInstance(pdfDocument, outputStream);
            pdfDocument.open();

            Paragraph titulo = new Paragraph("FarmaRedOps Intelligence - Reporte Dashboard");
            titulo.setAlignment(Element.ALIGN_CENTER);
            pdfDocument.add(titulo);
            pdfDocument.add(new Paragraph("Fecha de generacion: " + fechaGeneracion));
            pdfDocument.add(new Paragraph(" "));

            PdfPTable tablaResumen = new PdfPTable(2);
            tablaResumen.setWidthPercentage(100);
            agregarEncabezado(tablaResumen, "Metrica");
            agregarEncabezado(tablaResumen, "Valor");
            agregarFila(tablaResumen, "Medicamentos activos", String.valueOf(resumen.totalMedicamentosActivos()));
            agregarFila(tablaResumen, "Proveedores activos", String.valueOf(resumen.totalProveedoresActivos()));
            agregarFila(tablaResumen, "Ordenes de compra", String.valueOf(resumen.totalOrdenesCompra()));
            agregarFila(tablaResumen, "Inventarios registrados", String.valueOf(resumen.totalInventarios()));
            agregarFila(tablaResumen, "Cantidad total en inventario", String.valueOf(resumen.cantidadTotalInventario()));
            agregarFila(tablaResumen, "Stock critico", String.valueOf(resumen.totalStockCritico()));
            agregarFila(tablaResumen, "Lotes proximos a vencer", String.valueOf(resumen.totalLotesProximosVencer()));
            agregarFila(tablaResumen, "Ordenes pendientes", String.valueOf(resumen.totalOrdenesPendientes()));
            agregarFila(tablaResumen, "Alertas pendientes", String.valueOf(resumen.totalAlertasPendientes()));
            agregarFila(tablaResumen, "Valor ordenes pendientes", String.valueOf(resumen.valorTotalOrdenesPendientes()));
            pdfDocument.add(tablaResumen);

            pdfDocument.add(new Paragraph(" "));
            pdfDocument.add(new Paragraph("Ordenes por estado"));
            PdfPTable tablaEstados = new PdfPTable(2);
            tablaEstados.setWidthPercentage(100);
            agregarEncabezado(tablaEstados, "Estado");
            agregarEncabezado(tablaEstados, "Total");
            for (EstadoOrdenCompra estado : EstadoOrdenCompra.values()) {
                agregarFila(tablaEstados, estado.name(), String.valueOf(resumen.ordenesPorEstado().getOrDefault(estado, 0L)));
            }
            pdfDocument.add(tablaEstados);

            pdfDocument.add(new Paragraph(" "));
            pdfDocument.add(new Paragraph("Movimientos recientes"));
            PdfPTable tablaMovimientos = new PdfPTable(5);
            tablaMovimientos.setWidthPercentage(100);
            agregarEncabezado(tablaMovimientos, "Fecha");
            agregarEncabezado(tablaMovimientos, "Tipo");
            agregarEncabezado(tablaMovimientos, "Medicamento");
            agregarEncabezado(tablaMovimientos, "Centro");
            agregarEncabezado(tablaMovimientos, "Cantidad");

            for (MovimientoInventarioResponse movimiento : resumen.movimientosRecientes()) {
                agregarFila(tablaMovimientos, String.valueOf(movimiento.fechaMovimiento()));
                agregarFila(tablaMovimientos, String.valueOf(movimiento.tipoMovimiento()));
                agregarFila(tablaMovimientos, movimiento.medicamentoNombre());
                agregarFila(tablaMovimientos, movimiento.centroDistribucionNombre());
                agregarFila(tablaMovimientos, String.valueOf(movimiento.cantidad()));
            }

            pdfDocument.add(tablaMovimientos);
            pdfDocument.close();
        } catch (IOException | DocumentException exception) {
            throw new BadRequestException("No fue posible generar el reporte PDF de dashboard");
        }
    }

    private void agregarEncabezado(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void agregarFila(PdfPTable table, String etiqueta, String valor) {
        table.addCell(new Phrase(etiqueta));
        table.addCell(new Phrase(valor));
    }

    private void agregarFila(PdfPTable table, String valor) {
        table.addCell(new Phrase(valor != null ? valor : ""));
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("El archivo es obligatorio");
        }

        if (archivo.getSize() > maxSizeBytes) {
            throw new BadRequestException("El archivo supera el tamano maximo permitido");
        }

        String contentType = archivo.getContentType();

        if (!MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(contentType)) {
            throw new BadRequestException("Solo se permiten archivos PDF");
        }

        String nombreOriginal = archivo.getOriginalFilename();

        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            throw new BadRequestException("El nombre del archivo no es valido");
        }

        String nombreLimpio = StringUtils.cleanPath(nombreOriginal);

        if (!nombreLimpio.toLowerCase().endsWith(PDF_EXTENSION)) {
            throw new BadRequestException("El archivo debe tener extension .pdf");
        }

        if (nombreLimpio.contains("..")) {
            throw new BadRequestException("El nombre del archivo contiene una ruta invalida");
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

    private long obtenerTamanoArchivo(Path rutaArchivo) {
        try {
            return Files.size(rutaArchivo);
        } catch (IOException exception) {
            throw new BadRequestException("No fue posible leer el tamano del documento generado");
        }
    }

    private String normalizarUsuario(String usuarioGeneracion) {
        if (usuarioGeneracion == null || usuarioGeneracion.isBlank()) {
            return "sistema";
        }

        return usuarioGeneracion;
    }

    private Documento buscarDocumentoActivoPorId(Long id) {
        Documento documento = documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con ID: " + id));

        if (!Boolean.TRUE.equals(documento.getActivo())) {
            throw new ResourceNotFoundException("Documento no encontrado con ID: " + id);
        }

        return documento;
    }
}
