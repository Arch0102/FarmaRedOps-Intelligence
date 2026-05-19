package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.request.MovimientoInventarioRequest;
import com.farmared.opsintelligence.dto.response.MovimientoInventarioResponse;
import com.farmared.opsintelligence.service.MovimientoInventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movimientos-inventario")
@RequiredArgsConstructor
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoInventarioService;

    @PostMapping
    public ResponseEntity<MovimientoInventarioResponse> registrarMovimiento(
            @Valid @RequestBody MovimientoInventarioRequest request
    ) {
        MovimientoInventarioResponse response = movimientoInventarioService.registrarMovimiento(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoInventarioResponse> consultarPorId(@PathVariable Long id) {
        MovimientoInventarioResponse response = movimientoInventarioService.consultarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/kardex/inventario/{inventarioId}")
    public ResponseEntity<List<MovimientoInventarioResponse>> consultarKardexPorInventario(
            @PathVariable Long inventarioId
    ) {
        List<MovimientoInventarioResponse> response =
                movimientoInventarioService.consultarKardexPorInventario(inventarioId);

        return ResponseEntity.ok(response);
    }
}