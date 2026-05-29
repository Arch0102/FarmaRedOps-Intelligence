package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.response.UsuarioResponse;
import com.farmared.opsintelligence.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario, List<String> rolesUsuario) {
        List<String> roles = (rolesUsuario == null ? List.<String>of() : rolesUsuario)
                .stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(nombre -> !nombre.isBlank())
                .distinct()
                .toList();

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getNombreCompleto(),
                usuario.getActivo(),
                roles,
                usuario.getCreatedAt(),
                usuario.getUpdatedAt()
        );
    }
}
