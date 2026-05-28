package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.request.CentroDistribucionRequest;
import com.farmared.opsintelligence.dto.response.ApiResponse;
import com.farmared.opsintelligence.dto.response.CentroDistribucionResponse;
import com.farmared.opsintelligence.service.CentroDistribucionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/centros-distribucion")
@RequiredArgsConstructor
public class CentroDistribucionController {

    private final CentroDistribucionService centroDistribucionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CentroDistribucionResponse>>> listar(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Centros de distribucion consultados correctamente",
                request.getRequestURI(),
                centroDistribucionService.listar()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CentroDistribucionResponse>> consultarPorId(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Centro de distribucion consultado correctamente",
                request.getRequestURI(),
                centroDistribucionService.consultarPorId(id)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CentroDistribucionResponse>> crear(
            @Valid @RequestBody CentroDistribucionRequest request,
            HttpServletRequest servletRequest
    ) {
        CentroDistribucionResponse response = centroDistribucionService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Centro de distribucion creado correctamente", servletRequest.getRequestURI(), response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CentroDistribucionResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CentroDistribucionRequest request,
            HttpServletRequest servletRequest
    ) {
        CentroDistribucionResponse response = centroDistribucionService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Centro de distribucion actualizado correctamente", servletRequest.getRequestURI(), response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id, HttpServletRequest request) {
        centroDistribucionService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Centro de distribucion desactivado correctamente", request.getRequestURI(), null));
    }
}
