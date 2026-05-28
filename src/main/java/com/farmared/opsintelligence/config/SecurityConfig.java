package com.farmared.opsintelligence.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.farmared.opsintelligence.dto.response.ApiResponse;
import com.farmared.opsintelligence.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String AUXILIAR_BODEGA = "AUXILIAR_BODEGA";
    private static final String ANALISTA_COMPRAS = "ANALISTA_COMPRAS";
    private static final String ADMIN_AUDITOR = "ADMIN_AUDITOR";

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
                                writeErrorResponse(
                                        request,
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "No autorizado",
                                        "Autenticacion requerida"
                                )
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeErrorResponse(
                                        request,
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "Acceso denegado",
                                        accessDeniedException.getMessage()
                                )
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/categorias-medicamento/**",
                                "/api/v1/centros-distribucion/**",
                                "/api/v1/medicamentos/**"
                        ).hasAnyRole(AUXILIAR_BODEGA, ANALISTA_COMPRAS, ADMIN_AUDITOR)
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/categorias-medicamento/**",
                                "/api/v1/centros-distribucion/**",
                                "/api/v1/medicamentos/**"
                        ).hasRole(ADMIN_AUDITOR)
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/categorias-medicamento/**",
                                "/api/v1/centros-distribucion/**",
                                "/api/v1/medicamentos/**"
                        ).hasRole(ADMIN_AUDITOR)
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/categorias-medicamento/**",
                                "/api/v1/centros-distribucion/**",
                                "/api/v1/medicamentos/**"
                        ).hasRole(ADMIN_AUDITOR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/movimientos-inventario/**")
                        .hasAnyRole(AUXILIAR_BODEGA, ADMIN_AUDITOR)
                        .requestMatchers(HttpMethod.GET, "/api/v1/movimientos-inventario/**")
                        .hasAnyRole(AUXILIAR_BODEGA, ANALISTA_COMPRAS, ADMIN_AUDITOR)
                        .requestMatchers("/api/v1/proveedores/**", "/api/v1/ordenes-compra/**")
                        .hasAnyRole(ANALISTA_COMPRAS, ADMIN_AUDITOR)
                        .requestMatchers(HttpMethod.GET, "/api/v1/dashboard/**")
                        .hasAnyRole(ANALISTA_COMPRAS, ADMIN_AUDITOR)
                        .requestMatchers(HttpMethod.POST, "/api/v1/dashboard/**")
                        .hasRole(ADMIN_AUDITOR)
                        .requestMatchers("/api/v1/usuarios/**")
                        .hasRole(ADMIN_AUDITOR)
                        .requestMatchers("/api/v1/documentos/**")
                        .authenticated()
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

    private void writeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String message,
            String error
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getOutputStream(),
                ApiResponse.error(status, message, request.getRequestURI(), List.of(error))
        );
    }
}
