package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.request.ProveedorRequest;
import com.farmared.opsintelligence.dto.response.ProveedorResponse;
import com.farmared.opsintelligence.service.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @PostMapping
    public ResponseEntity<ProveedorResponse> crearProveedor(
            @Valid @RequestBody ProveedorRequest request
    ) {
        ProveedorResponse response = proveedorService.crearProveedor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorResponse> actualizarProveedor(
            @PathVariable Long id,
            @Valid @RequestBody ProveedorRequest request
    ) {
        ProveedorResponse response = proveedorService.actualizarProveedor(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponse> consultarPorId(@PathVariable Long id) {
        ProveedorResponse response = proveedorService.consultarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProveedorResponse>> listarTodos() {
        List<ProveedorResponse> response = proveedorService.listarTodos();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<ProveedorResponse>> listarActivos() {
        List<ProveedorResponse> response = proveedorService.listarActivos();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarProveedor(@PathVariable Long id) {
        proveedorService.desactivarProveedor(id);
        return ResponseEntity.noContent().build();
    }
}