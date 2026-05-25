package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.response.UsuarioResponse;
import com.farmared.opsintelligence.entity.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponse toResponse(Usuario usuario);
}
