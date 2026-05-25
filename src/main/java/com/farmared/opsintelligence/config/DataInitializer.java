package com.farmared.opsintelligence.config;

import com.farmared.opsintelligence.entity.Rol;
import com.farmared.opsintelligence.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;

    @Override
    @Transactional
    public void run(String... args) {
        crearRolSiNoExiste("ROLE_AUXILIAR_BODEGA", "Auxiliar de Bodega");
        crearRolSiNoExiste("ROLE_ANALISTA_COMPRAS", "Analista de Compras");
        crearRolSiNoExiste("ROLE_ADMIN_AUDITOR", "Administrador / Auditor");
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
