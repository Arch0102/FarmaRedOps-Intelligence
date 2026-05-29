package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.request.DocumentoUpdateRequest;
import com.farmared.opsintelligence.dto.request.DocumentoUploadRequest;
import com.farmared.opsintelligence.dto.response.DocumentoResponse;
import com.farmared.opsintelligence.service.DocumentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoResponse> subirDocumento(
            @RequestPart("archivo") MultipartFile archivo,
            @Valid @RequestPart("metadata") DocumentoUploadRequest request
    ) {
        DocumentoResponse response = documentoService.subirDocumento(archivo, request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentoResponse>> listarDocumentosActivos() {
        return ResponseEntity.ok(documentoService.listarDocumentosActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentoResponse> consultarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(documentoService.consultarPorId(id));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> descargarDocumento(@PathVariable Long id) {
        DocumentoResponse documento = documentoService.consultarPorId(id);
        Resource resource = documentoService.descargarDocumento(id);

        ContentDisposition contentDisposition = ContentDisposition
                .attachment()
                .filename(documento.nombreOriginal())
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(resource);
    }

    @PostMapping(value = "/generar/dashboard", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generarReporteDashboard() {
        byte[] pdf = documentoService.generarReporteDashboardPdf();
        ContentDisposition contentDisposition = ContentDisposition
                .attachment()
                .filename("reporte-dashboard.pdf")
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(pdf);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentoResponse> actualizarDocumento(
            @PathVariable Long id,
            @Valid @RequestBody DocumentoUpdateRequest request
    ) {
        return ResponseEntity.ok(documentoService.actualizarDocumento(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDocumento(@PathVariable Long id) {
        documentoService.eliminarDocumento(id);
        return ResponseEntity.noContent().build();
    }
}
