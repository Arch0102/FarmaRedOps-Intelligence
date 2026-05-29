package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.request.OrdenCompraRequest;
import com.farmared.opsintelligence.dto.response.OrdenCompraResponse;
import com.farmared.opsintelligence.entity.enums.EstadoOrdenCompra;
import com.farmared.opsintelligence.service.OrdenCompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes-compra")
@RequiredArgsConstructor
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;

    @PostMapping
    public ResponseEntity<OrdenCompraResponse> crearOrdenCompra(
            @Valid @RequestBody OrdenCompraRequest request
    ) {
        OrdenCompraResponse response = ordenCompraService.crearOrdenCompra(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenCompraResponse> consultarPorId(@PathVariable Long id) {
        OrdenCompraResponse response = ordenCompraService.consultarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrdenCompraResponse>> listarTodas() {
        List<OrdenCompraResponse> response = ordenCompraService.listarTodas();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<OrdenCompraResponse>> listarPorEstado(
            @PathVariable EstadoOrdenCompra estado
    ) {
        List<OrdenCompraResponse> response = ordenCompraService.listarPorEstado(estado);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/estado/{nuevoEstado}")
    public ResponseEntity<OrdenCompraResponse> cambiarEstado(
            @PathVariable Long id,
            @PathVariable EstadoOrdenCompra nuevoEstado
    ) {
        OrdenCompraResponse response = ordenCompraService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.ok(response);
    }
}