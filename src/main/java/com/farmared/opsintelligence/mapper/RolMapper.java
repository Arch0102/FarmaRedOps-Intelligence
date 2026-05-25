package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.response.RolResponse;
import com.farmared.opsintelligence.entity.Rol;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RolMapper {

    RolResponse toResponse(Rol rol);
}
