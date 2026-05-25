package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.request.DocumentoUpdateRequest;
import com.farmared.opsintelligence.dto.request.DocumentoUploadRequest;
import com.farmared.opsintelligence.dto.response.ApiResponse;
import com.farmared.opsintelligence.dto.response.DocumentoResponse;
import com.farmared.opsintelligence.service.DocumentoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentoResponse>> subirDocumento(
            @RequestPart("archivo") MultipartFile archivo,
            @Valid @RequestPart("metadata") DocumentoUploadRequest request,
            HttpServletRequest servletRequest
    ) {
        DocumentoResponse response = documentoService.subirDocumento(archivo, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED,
                        "Registro creado correctamente",
                        servletRequest.getRequestURI(),
                        response
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentoResponse>>> listarDocumentosActivos(HttpServletRequest request) {
        List<DocumentoResponse> response = documentoService.listarDocumentosActivos();
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Registros consultados correctamente",
                request.getRequestURI(),
                response
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentoResponse>> consultarPorId(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        DocumentoResponse response = documentoService.consultarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Registro consultado correctamente",
                request.getRequestURI(),
                response
        ));
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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentoResponse>> actualizarDocumento(
            @PathVariable Long id,
            @Valid @RequestBody DocumentoUpdateRequest request,
            HttpServletRequest servletRequest
    ) {
        DocumentoResponse response = documentoService.actualizarDocumento(id, request);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Registro actualizado correctamente",
                servletRequest.getRequestURI(),
                response
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarDocumento(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        documentoService.eliminarDocumento(id);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Registro eliminado correctamente",
                request.getRequestURI(),
                null
        ));
    }
}
