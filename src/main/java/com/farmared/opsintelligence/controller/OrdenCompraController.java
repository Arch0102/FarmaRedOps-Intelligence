package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.request.OrdenCompraRequest;
import com.farmared.opsintelligence.dto.response.ApiResponse;
import com.farmared.opsintelligence.dto.response.OrdenCompraResponse;
import com.farmared.opsintelligence.entity.enums.EstadoOrdenCompra;
import com.farmared.opsintelligence.service.OrdenCompraService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes-compra")
@RequiredArgsConstructor
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrdenCompraResponse>> crearOrdenCompra(
            @Valid @RequestBody OrdenCompraRequest request,
            HttpServletRequest servletRequest
    ) {
        OrdenCompraResponse response = ordenCompraService.crearOrdenCompra(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Orden de compra creada correctamente", servletRequest.getRequestURI(), response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrdenCompraResponse>> consultarPorId(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        OrdenCompraResponse response = ordenCompraService.consultarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Orden de compra consultada correctamente", request.getRequestURI(), response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrdenCompraResponse>>> listarTodas(HttpServletRequest request) {
        List<OrdenCompraResponse> response = ordenCompraService.listarTodas();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Ordenes de compra consultadas correctamente", request.getRequestURI(), response));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<OrdenCompraResponse>>> listarPorEstado(
            @PathVariable EstadoOrdenCompra estado,
            HttpServletRequest request
    ) {
        List<OrdenCompraResponse> response = ordenCompraService.listarPorEstado(estado);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Ordenes de compra consultadas por estado correctamente", request.getRequestURI(), response));
    }

    @PatchMapping("/{id}/estado/{nuevoEstado}")
    public ResponseEntity<ApiResponse<OrdenCompraResponse>> cambiarEstado(
            @PathVariable Long id,
            @PathVariable EstadoOrdenCompra nuevoEstado,
            HttpServletRequest request
    ) {
        OrdenCompraResponse response = ordenCompraService.cambiarEstado(id, nuevoEstado);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Estado de orden actualizado correctamente", request.getRequestURI(), response));
    }
}
