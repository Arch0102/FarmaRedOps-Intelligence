package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.request.ProveedorRequest;
import com.farmared.opsintelligence.dto.response.ApiResponse;
import com.farmared.opsintelligence.dto.response.ProveedorResponse;
import com.farmared.opsintelligence.service.ProveedorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProveedorResponse>> crearProveedor(
            @Valid @RequestBody ProveedorRequest request,
            HttpServletRequest servletRequest
    ) {
        ProveedorResponse response = proveedorService.crearProveedor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Proveedor creado correctamente", servletRequest.getRequestURI(), response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponse>> actualizarProveedor(
            @PathVariable Long id,
            @Valid @RequestBody ProveedorRequest request,
            HttpServletRequest servletRequest
    ) {
        ProveedorResponse response = proveedorService.actualizarProveedor(id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Proveedor actualizado correctamente", servletRequest.getRequestURI(), response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponse>> consultarPorId(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        ProveedorResponse response = proveedorService.consultarPorId(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Proveedor consultado correctamente", request.getRequestURI(), response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProveedorResponse>>> listarTodos(HttpServletRequest request) {
        List<ProveedorResponse> response = proveedorService.listarTodos();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Proveedores consultados correctamente", request.getRequestURI(), response));
    }

    @GetMapping("/activos")
    public ResponseEntity<ApiResponse<List<ProveedorResponse>>> listarActivos(HttpServletRequest request) {
        List<ProveedorResponse> response = proveedorService.listarActivos();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Proveedores activos consultados correctamente", request.getRequestURI(), response));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<Void>> desactivarProveedor(@PathVariable Long id, HttpServletRequest request) {
        proveedorService.desactivarProveedor(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Proveedor desactivado correctamente", request.getRequestURI(), null));
    }
}
