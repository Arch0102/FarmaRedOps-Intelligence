package com.farmared.opsintelligence.mapper;

import com.farmared.opsintelligence.dto.response.DocumentoResponse;
import com.farmared.opsintelligence.entity.Documento;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentoMapper {

    DocumentoResponse toResponse(Documento documento);
}
