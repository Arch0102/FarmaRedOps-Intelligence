package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.response.ApiResponse;
import com.farmared.opsintelligence.dto.response.LoteMedicamentoResponse;
import com.farmared.opsintelligence.service.LoteMedicamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lotes-medicamento")
@RequiredArgsConstructor
public class LoteMedicamentoController {

    private final LoteMedicamentoService loteMedicamentoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoteMedicamentoResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Lotes consultados correctamente", loteMedicamentoService.listar()));
    }

    @GetMapping("/medicamento/{medicamentoId}")
    public ResponseEntity<ApiResponse<List<LoteMedicamentoResponse>>> listarPorMedicamento(
            @PathVariable Long medicamentoId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Lotes del medicamento consultados correctamente",
                loteMedicamentoService.listarPorMedicamento(medicamentoId)
        ));
    }
}
