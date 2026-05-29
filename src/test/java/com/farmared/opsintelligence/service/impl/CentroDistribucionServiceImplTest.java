package com.farmared.opsintelligence.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.farmared.opsintelligence.dto.request.CentroDistribucionRequest;
import com.farmared.opsintelligence.dto.response.CentroDistribucionResponse;
import com.farmared.opsintelligence.entity.CentroDistribucion;
import com.farmared.opsintelligence.exception.ResourceNotFoundException;
import com.farmared.opsintelligence.mapper.CentroDistribucionMapper;
import com.farmared.opsintelligence.repository.CentroDistribucionRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CentroDistribucionServiceImplTest {

    @Mock
    private CentroDistribucionRepository centroDistribucionRepository;

    @Mock
    private CentroDistribucionMapper centroDistribucionMapper;

    @InjectMocks
    private CentroDistribucionServiceImpl centroDistribucionService;

    @Test
    void crearCentroCorrectamenteActivaPorDefecto() {
        // Arrange
        CentroDistribucionRequest request = request("CD-001", "Centro Principal", true);
        CentroDistribucion mappedEntity = centro("CD-001", "Centro Principal", null);
        CentroDistribucion savedEntity = centro("CD-001", "Centro Principal", true);
        savedEntity.setId(1L);
        CentroDistribucionResponse expectedResponse = response(savedEntity);

        when(centroDistribucionRepository.existsByCodigo("CD-001")).thenReturn(false);
        when(centroDistribucionMapper.toEntity(request)).thenReturn(mappedEntity);
        when(centroDistribucionRepository.save(any(CentroDistribucion.class))).thenReturn(savedEntity);
        when(centroDistribucionMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        // Act
        CentroDistribucionResponse response = centroDistribucionService.crear(request);

        // Assert
        ArgumentCaptor<CentroDistribucion> centroCaptor = ArgumentCaptor.forClass(CentroDistribucion.class);
        verify(centroDistribucionRepository).save(centroCaptor.capture());

        assertTrue(centroCaptor.getValue().getActivo());
        assertEquals("CD-001", response.codigo());
        assertEquals("Centro Principal", response.nombre());
    }

    @Test
    void actualizarCentroMantieneCodigoCuandoSeActualizanDatosOperativos() {
        // Arrange
        CentroDistribucionRequest request = new CentroDistribucionRequest(
                "CD-001",
                "Centro Principal Actualizado",
                "Avenida 10 # 20-30",
                "Bogota",
                true
        );
        CentroDistribucion existing = centro("CD-001", "Centro Principal", true);
        existing.setId(1L);

        when(centroDistribucionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(centroDistribucionRepository.findByCodigo("CD-001")).thenReturn(Optional.of(existing));
        when(centroDistribucionRepository.save(any(CentroDistribucion.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(centroDistribucionMapper.toResponse(any(CentroDistribucion.class))).thenAnswer(invocation -> {
            CentroDistribucion centro = invocation.getArgument(0);
            return response(centro);
        });

        // Act
        CentroDistribucionResponse response = centroDistribucionService.actualizar(1L, request);

        // Assert
        assertEquals("CD-001", response.codigo());
        assertEquals("Centro Principal Actualizado", response.nombre());
        assertEquals("Avenida 10 # 20-30", existing.getDireccion());
        assertEquals("Bogota", existing.getCiudad());
    }

    @Test
    void eliminarDesactivaCentro() {
        // Arrange
        CentroDistribucion existing = centro("CD-001", "Centro Principal", true);
        existing.setId(1L);

        when(centroDistribucionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(centroDistribucionRepository.save(any(CentroDistribucion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        centroDistribucionService.eliminar(1L);

        // Assert
        ArgumentCaptor<CentroDistribucion> centroCaptor = ArgumentCaptor.forClass(CentroDistribucion.class);
        verify(centroDistribucionRepository).save(centroCaptor.capture());
        assertFalse(centroCaptor.getValue().getActivo());
    }

    @Test
    void consultarPorIdLanzaResourceNotFoundExceptionSiNoExiste() {
        // Arrange
        when(centroDistribucionRepository.findById(99L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThrows(ResourceNotFoundException.class, () -> centroDistribucionService.consultarPorId(99L));
    }

    private static CentroDistribucionRequest request(String codigo, String nombre, Boolean activo) {
        return new CentroDistribucionRequest(codigo, nombre, "Calle 1 # 2-3", "Bogota", activo);
    }

    private static CentroDistribucion centro(String codigo, String nombre, Boolean activo) {
        CentroDistribucion centro = new CentroDistribucion();
        centro.setCodigo(codigo);
        centro.setNombre(nombre);
        centro.setDireccion("Calle 1 # 2-3");
        centro.setCiudad("Bogota");
        centro.setActivo(activo);
        return centro;
    }

    private static CentroDistribucionResponse response(CentroDistribucion centro) {
        return new CentroDistribucionResponse(
                centro.getId(),
                centro.getCodigo(),
                centro.getNombre(),
                centro.getDireccion(),
                centro.getCiudad(),
                centro.getActivo(),
                centro.getCreatedAt(),
                centro.getUpdatedAt()
        );
    }
}
