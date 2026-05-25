package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.response.UsuarioRolResponse;
import com.farmared.opsintelligence.entity.UsuarioRol;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioRolMapper {

    @Mapping(source = "usuario.id", target = "usuarioId")
    @Mapping(source = "usuario.username", target = "username")
    @Mapping(source = "rol.id", target = "rolId")
    @Mapping(source = "rol.nombre", target = "rolNombre")
    UsuarioRolResponse toResponse(UsuarioRol usuarioRol);
}
