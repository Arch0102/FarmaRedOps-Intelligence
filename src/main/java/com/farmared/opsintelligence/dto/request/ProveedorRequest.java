package com.farmared.opsintelligence.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProveedorRequest(

        @NotBlank(message = "El NIT es obligatorio")
        @Size(max = 30, message = "El NIT no puede superar los 30 caracteres")
        String nit,

        @NotBlank(message = "El nombre del proveedor es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String nombre,

        @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
        String telefono,

        @Email(message = "El correo debe tener un formato válido")
        @Size(max = 150, message = "El correo no puede superar los 150 caracteres")
        String correo,

        @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
        String direccion,

        Boolean activo
) {
}