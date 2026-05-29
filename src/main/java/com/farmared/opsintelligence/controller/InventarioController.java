package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.response.ApiResponse;
import com.farmared.opsintelligence.dto.response.InventarioResponse;
import com.farmared.opsintelligence.dto.response.InventarioResumenResponse;
import com.farmared.opsintelligence.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventarios")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventarioResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Inventarios consultados correctamente", inventarioService.listar()));
    }

    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<InventarioResumenResponse>> obtenerResumen() {
        return ResponseEntity.ok(ApiResponse.ok("Resumen de inventario consultado correctamente", inventarioService.obtenerResumen()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventarioResponse>> consultarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Inventario consultado correctamente", inventarioService.consultarPorId(id)));
    }
}
