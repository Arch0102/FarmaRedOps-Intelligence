package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.request.CategoriaMedicamentoRequest;
import com.farmared.opsintelligence.dto.response.ApiResponse;
import com.farmared.opsintelligence.dto.response.CategoriaMedicamentoResponse;
import com.farmared.opsintelligence.service.CategoriaMedicamentoService;
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
@RequestMapping("/api/v1/categorias-medicamento")
@RequiredArgsConstructor
public class CategoriaMedicamentoController {

    private final CategoriaMedicamentoService categoriaMedicamentoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaMedicamentoResponse>>> listar(HttpServletRequest request) {
        List<CategoriaMedicamentoResponse> response = categoriaMedicamentoService.listar();
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Registros consultados correctamente",
                request.getRequestURI(),
                response
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaMedicamentoResponse>> consultarPorId(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        CategoriaMedicamentoResponse response = categoriaMedicamentoService.consultarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Registro consultado correctamente",
                request.getRequestURI(),
                response
        ));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoriaMedicamentoResponse>> crear(
            @Valid @RequestBody CategoriaMedicamentoRequest request,
            HttpServletRequest servletRequest
    ) {
        CategoriaMedicamentoResponse response = categoriaMedicamentoService.crear(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED,
                        "Registro creado correctamente",
                        servletRequest.getRequestURI(),
                        response
                ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaMedicamentoResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaMedicamentoRequest request,
            HttpServletRequest servletRequest
    ) {
        CategoriaMedicamentoResponse response = categoriaMedicamentoService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Registro actualizado correctamente",
                servletRequest.getRequestURI(),
                response
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        categoriaMedicamentoService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Registro eliminado correctamente",
                request.getRequestURI(),
                null
        ));
    }
}
