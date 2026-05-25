package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.request.MovimientoInventarioRequest;
import com.farmared.opsintelligence.dto.response.ApiResponse;
import com.farmared.opsintelligence.dto.response.MovimientoInventarioResponse;
import com.farmared.opsintelligence.service.MovimientoInventarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movimientos-inventario")
@RequiredArgsConstructor
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoInventarioService;

    @PostMapping
    public ResponseEntity<ApiResponse<MovimientoInventarioResponse>> registrarMovimiento(
            @Valid @RequestBody MovimientoInventarioRequest request,
            HttpServletRequest servletRequest
    ) {
        MovimientoInventarioResponse response = movimientoInventarioService.registrarMovimiento(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED,
                        "Registro creado correctamente",
                        servletRequest.getRequestURI(),
                        response
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovimientoInventarioResponse>> consultarPorId(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        MovimientoInventarioResponse response = movimientoInventarioService.consultarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Registro consultado correctamente",
                request.getRequestURI(),
                response
        ));
    }

    @GetMapping("/kardex/inventario/{inventarioId}")
    public ResponseEntity<ApiResponse<List<MovimientoInventarioResponse>>> consultarKardexPorInventario(
            @PathVariable Long inventarioId,
            HttpServletRequest request
    ) {
        List<MovimientoInventarioResponse> response =
                movimientoInventarioService.consultarKardexPorInventario(inventarioId);

        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK,
                "Registros consultados correctamente",
                request.getRequestURI(),
                response
        ));
    }
}
