package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.request.MedicamentoRequest;
import com.farmared.opsintelligence.dto.response.MedicamentoResponse;
import com.farmared.opsintelligence.service.MedicamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/medicamentos")
@RequiredArgsConstructor
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    @GetMapping
    public ResponseEntity<List<MedicamentoResponse>> listar() {
        return ResponseEntity.ok(medicamentoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicamentoResponse> consultarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(medicamentoService.consultarPorId(id));
    }

    @PostMapping
    public ResponseEntity<MedicamentoResponse> crear(
            @Valid @RequestBody MedicamentoRequest request
    ) {
        MedicamentoResponse response = medicamentoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicamentoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MedicamentoRequest request
    ) {
        return ResponseEntity.ok(medicamentoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        medicamentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}