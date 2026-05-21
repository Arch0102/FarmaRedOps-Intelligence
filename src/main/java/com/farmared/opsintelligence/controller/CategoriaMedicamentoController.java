package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.request.CategoriaMedicamentoRequest;
import com.farmared.opsintelligence.dto.response.CategoriaMedicamentoResponse;
import com.farmared.opsintelligence.service.CategoriaMedicamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias-medicamento")
@RequiredArgsConstructor
public class CategoriaMedicamentoController {

    private final CategoriaMedicamentoService categoriaMedicamentoService;

    @GetMapping
    public ResponseEntity<List<CategoriaMedicamentoResponse>> listar() {
        return ResponseEntity.ok(categoriaMedicamentoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaMedicamentoResponse> consultarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(categoriaMedicamentoService.consultarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CategoriaMedicamentoResponse> crear(
            @Valid @RequestBody CategoriaMedicamentoRequest request
    ) {
        CategoriaMedicamentoResponse response = categoriaMedicamentoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaMedicamentoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaMedicamentoRequest request
    ) {
        return ResponseEntity.ok(categoriaMedicamentoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaMedicamentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
