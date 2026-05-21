package com.farmared.opsintelligence.service;

import com.farmared.opsintelligence.dto.request.DocumentoUpdateRequest;
import com.farmared.opsintelligence.dto.request.DocumentoUploadRequest;
import com.farmared.opsintelligence.dto.response.DocumentoResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentoService {

    DocumentoResponse subirDocumento(MultipartFile archivo, DocumentoUploadRequest request);

    List<DocumentoResponse> listarDocumentosActivos();

    DocumentoResponse consultarPorId(Long id);

    Resource descargarDocumento(Long id);

    DocumentoResponse actualizarDocumento(Long id, DocumentoUpdateRequest request);

    void eliminarDocumento(Long id);
}
