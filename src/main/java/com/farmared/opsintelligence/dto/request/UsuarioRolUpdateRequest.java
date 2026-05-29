package com.farmared.opsintelligence.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UsuarioRolUpdateRequest(
        @NotEmpty(message = "El usuario debe tener al menos un rol")
        List<@NotBlank(message = "El nombre del rol no puede estar vacio") String> roles
) {
}
