package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.response.AlertaStockResponse;
import com.farmared.opsintelligence.dto.response.ApiResponse;
import com.farmared.opsintelligence.service.AlertaStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alertas-stock")
@RequiredArgsConstructor
public class AlertaStockController {

    private final AlertaStockService alertaStockService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertaStockResponse>>> listarPendientes() {
        return ResponseEntity.ok(ApiResponse.ok("Alertas pendientes consultadas correctamente", alertaStockService.listarPendientes()));
    }

    @PatchMapping("/{id}/resolver")
    public ResponseEntity<ApiResponse<AlertaStockResponse>> resolver(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Alerta resuelta correctamente", alertaStockService.resolver(id)));
    }
}
