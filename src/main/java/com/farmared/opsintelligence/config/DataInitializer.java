package com.farmared.opsintelligence.config;

import com.farmared.opsintelligence.entity.Rol;
import com.farmared.opsintelligence.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RolRepository rolRepository;

    @Bean
    public CommandLineRunner initializeRoles() {
        return args -> {
            crearRolSiNoExiste("ROLE_AUXILIAR_BODEGA", "Gestiona movimientos y consultas de inventario");
            crearRolSiNoExiste("ROLE_ANALISTA_COMPRAS", "Consulta analitica, proveedores y ordenes de compra");
            crearRolSiNoExiste("ROLE_ADMIN_AUDITOR", "Administra catalogos, usuarios y auditoria del sistema");
        };
    }

    private void crearRolSiNoExiste(String nombre, String descripcion) {
        if (rolRepository.existsByNombre(nombre)) {
            return;
        }

        Rol rol = new Rol();
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        rol.setActivo(true);
        rolRepository.save(rol);
    }
}
