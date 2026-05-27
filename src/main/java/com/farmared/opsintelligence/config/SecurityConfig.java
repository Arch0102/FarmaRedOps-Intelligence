package com.farmared.opsintelligence.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmared.opsintelligence.dto.response.ApiResponse;
import com.farmared.opsintelligence.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                writeSecurityError(
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "No autorizado",
                                        request.getRequestURI(),
                                        authException.getMessage()
                                )
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeSecurityError(
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "Acceso denegado",
                                        request.getRequestURI(),
                                        accessDeniedException.getMessage()
                                )
                        )
                )
                .authorizeHttpRequests(auth -> auth

                        // Endpoints públicos de autenticación.
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Inventario físico:
                        // Auxiliar de Bodega puede registrar entradas y salidas.
                        // Administrador/Auditor también puede hacerlo por supervisión.
                        .requestMatchers(HttpMethod.POST, "/api/v1/movimientos-inventario/**")
                        .hasAnyRole("AUXILIAR_BODEGA", "ADMIN_AUDITOR")

                        .requestMatchers(HttpMethod.GET, "/api/v1/movimientos-inventario/**")
                        .hasAnyRole("AUXILIAR_BODEGA", "ANALISTA_COMPRAS", "ADMIN_AUDITOR")

                        // Medicamentos:
                        // Todos los roles definidos pueden consultar.
                        // Solo Administrador/Auditor puede crear, actualizar o eliminar registros maestros.
                        .requestMatchers(HttpMethod.GET, "/api/v1/medicamentos/**")
                        .hasAnyRole("AUXILIAR_BODEGA", "ANALISTA_COMPRAS", "ADMIN_AUDITOR")

                        .requestMatchers(HttpMethod.POST, "/api/v1/medicamentos/**")
                        .hasRole("ADMIN_AUDITOR")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/medicamentos/**")
                        .hasRole("ADMIN_AUDITOR")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/medicamentos/**")
                        .hasRole("ADMIN_AUDITOR")

                        // Categorías de medicamento:
                        // Son registros maestros. La consulta es general, pero la modificación solo es de administración.
                        .requestMatchers(HttpMethod.GET, "/api/v1/categorias-medicamento/**")
                        .hasAnyRole("AUXILIAR_BODEGA", "ANALISTA_COMPRAS", "ADMIN_AUDITOR")

                        .requestMatchers(HttpMethod.POST, "/api/v1/categorias-medicamento/**")
                        .hasRole("ADMIN_AUDITOR")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/categorias-medicamento/**")
                        .hasRole("ADMIN_AUDITOR")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/categorias-medicamento/**")
                        .hasRole("ADMIN_AUDITOR")

                        // Centros de distribución:
                        // Todos pueden consultar. Solo Administrador/Auditor puede modificar.
                        .requestMatchers(HttpMethod.GET, "/api/v1/centros-distribucion/**")
                        .hasAnyRole("AUXILIAR_BODEGA", "ANALISTA_COMPRAS", "ADMIN_AUDITOR")

                        .requestMatchers(HttpMethod.POST, "/api/v1/centros-distribucion/**")
                        .hasRole("ADMIN_AUDITOR")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/centros-distribucion/**")
                        .hasRole("ADMIN_AUDITOR")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/centros-distribucion/**")
                        .hasRole("ADMIN_AUDITOR")

                        // Documentos:
                        // Por ahora se permite a cualquier usuario autenticado.
                        .requestMatchers("/api/v1/documentos/**")
                        .authenticated()

                        // Proveedores:
                        // El Analista de Compras gestiona proveedores.
                        // Administrador/Auditor también tiene acceso.
                        .requestMatchers("/api/v1/proveedores/**")
                        .hasAnyRole("ANALISTA_COMPRAS", "ADMIN_AUDITOR")

                        // Órdenes de compra:
                        // Corresponden al rol de compras y administración.
                        .requestMatchers("/api/v1/ordenes-compra/**")
                        .hasAnyRole("ANALISTA_COMPRAS", "ADMIN_AUDITOR")

                        // Dashboard:
                        // Analista de Compras puede consultar métricas operativas.
                        // Administrador/Auditor puede consultar y administrar métricas.
                        .requestMatchers(HttpMethod.GET, "/api/v1/dashboard/**")
                        .hasAnyRole("ANALISTA_COMPRAS", "ADMIN_AUDITOR")

                        .requestMatchers(HttpMethod.POST, "/api/v1/dashboard/**")
                        .hasRole("ADMIN_AUDITOR")

                        .requestMatchers(HttpMethod.PUT, "/api/v1/dashboard/**")
                        .hasRole("ADMIN_AUDITOR")

                        .requestMatchers(HttpMethod.DELETE, "/api/v1/dashboard/**")
                        .hasRole("ADMIN_AUDITOR")

                        // Usuarios:
                        // Solo Administrador/Auditor puede gestionar usuarios.
                        .requestMatchers("/api/v1/usuarios/**")
                        .hasRole("ADMIN_AUDITOR")

                        // Cualquier otro endpoint requiere usuario autenticado.
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void writeSecurityError(
            HttpServletResponse response,
            HttpStatus status,
            String message,
            String path,
            String error
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.error(status, message, path, Map.of("error", error))
        );
    }
}