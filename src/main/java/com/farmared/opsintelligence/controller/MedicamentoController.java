package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.request.MedicamentoRequest;
import com.farmared.opsintelligence.dto.response.ApiResponse;
import com.farmared.opsintelligence.dto.response.MedicamentoResponse;
import com.farmared.opsintelligence.service.MedicamentoService;
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
@RequestMapping("/api/v1/medicamentos")
@RequiredArgsConstructor
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicamentoResponse>>> listar(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Medicamentos consultados correctamente",
                request.getRequestURI(),
                medicamentoService.listar()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicamentoResponse>> consultarPorId(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Medicamento consultado correctamente",
                request.getRequestURI(),
                medicamentoService.consultarPorId(id)
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MedicamentoResponse>> crear(
            @Valid @RequestBody MedicamentoRequest request,
            HttpServletRequest servletRequest
    ) {
        MedicamentoResponse response = medicamentoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Medicamento creado correctamente", servletRequest.getRequestURI(), response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicamentoResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MedicamentoRequest request,
            HttpServletRequest servletRequest
    ) {
        MedicamentoResponse response = medicamentoService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Medicamento actualizado correctamente", servletRequest.getRequestURI(), response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id, HttpServletRequest request) {
        medicamentoService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Medicamento desactivado correctamente", request.getRequestURI(), null));
    }
}
