package com.farmared.opsintelligence.service;

import com.farmared.opsintelligence.dto.request.UsuarioEstadoUpdateRequest;
import com.farmared.opsintelligence.dto.request.UsuarioRolUpdateRequest;
import com.farmared.opsintelligence.dto.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    List<UsuarioResponse> listar();

    UsuarioResponse consultarPorId(Long id);

    UsuarioResponse actualizarRoles(Long id, UsuarioRolUpdateRequest request);

    UsuarioResponse actualizarEstado(Long id, UsuarioEstadoUpdateRequest request);
}
