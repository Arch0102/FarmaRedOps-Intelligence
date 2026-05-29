package com.farmared.opsintelligence.controller;

import com.farmared.opsintelligence.dto.request.UsuarioEstadoUpdateRequest;
import com.farmared.opsintelligence.dto.request.UsuarioRolUpdateRequest;
import com.farmared.opsintelligence.dto.response.ApiResponse;
import com.farmared.opsintelligence.dto.response.UsuarioResponse;
import com.farmared.opsintelligence.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> listar() {
        List<UsuarioResponse> usuarios = usuarioService.listar();
        return ResponseEntity.ok(ApiResponse.ok("Usuarios consultados correctamente", usuarios));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> consultarPorId(@PathVariable Long id) {
        UsuarioResponse usuario = usuarioService.consultarPorId(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario consultado correctamente", usuario));
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarRoles(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRolUpdateRequest request
    ) {
        UsuarioResponse usuario = usuarioService.actualizarRoles(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Roles actualizados correctamente", usuario));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioEstadoUpdateRequest request
    ) {
        UsuarioResponse usuario = usuarioService.actualizarEstado(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Estado del usuario actualizado correctamente", usuario));
    }
}
