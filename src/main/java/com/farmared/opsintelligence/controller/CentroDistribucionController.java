package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.request.CentroDistribucionRequest;
import com.farmared.opsintelligence.dto.response.ApiResponse;
import com.farmared.opsintelligence.dto.response.CentroDistribucionResponse;
import com.farmared.opsintelligence.service.CentroDistribucionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/centros-distribucion")
@RequiredArgsConstructor
public class CentroDistribucionController {

    private final CentroDistribucionService centroDistribucionService;

    @GetMapping
    public ResponseEntity<List<CentroDistribucionResponse>> listar() {
        return ResponseEntity.ok(centroDistribucionService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CentroDistribucionResponse> consultarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(centroDistribucionService.consultarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CentroDistribucionResponse> crear(
            @Valid @RequestBody CentroDistribucionRequest request
    ) {
        CentroDistribucionResponse response = centroDistribucionService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CentroDistribucionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CentroDistribucionRequest request
    ) {
        return ResponseEntity.ok(centroDistribucionService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        centroDistribucionService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Centro eliminado correctamente", null));
    }
}
