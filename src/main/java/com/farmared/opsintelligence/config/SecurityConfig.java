package com.farmared.opsintelligence.config;

import com.farmared.opsintelligence.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/v1/usuarios/**").hasRole("ADMIN_AUDITOR")
                        .requestMatchers("/api/v1/dashboard/**").hasAnyRole("ANALISTA_COMPRAS", "ADMIN_AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/medicamentos/**").hasAnyRole("AUXILIAR_BODEGA", "ANALISTA_COMPRAS", "ADMIN_AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/categorias-medicamento/**").hasAnyRole("AUXILIAR_BODEGA", "ANALISTA_COMPRAS", "ADMIN_AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/centros-distribucion/**").hasAnyRole("AUXILIAR_BODEGA", "ANALISTA_COMPRAS", "ADMIN_AUDITOR")
                        .requestMatchers("/api/v1/medicamentos/**").hasRole("ADMIN_AUDITOR")
                        .requestMatchers("/api/v1/categorias-medicamento/**").hasRole("ADMIN_AUDITOR")
                        .requestMatchers("/api/v1/centros-distribucion/**").hasRole("ADMIN_AUDITOR")
                        .requestMatchers("/api/v1/proveedores/**").hasAnyRole("ANALISTA_COMPRAS", "ADMIN_AUDITOR")
                        .requestMatchers("/api/v1/ordenes-compra/**").hasAnyRole("ANALISTA_COMPRAS", "ADMIN_AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/inventarios/**").hasAnyRole("AUXILIAR_BODEGA", "ANALISTA_COMPRAS", "ADMIN_AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/lotes-medicamento/**").hasAnyRole("AUXILIAR_BODEGA", "ANALISTA_COMPRAS", "ADMIN_AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/alertas-stock/**").hasAnyRole("AUXILIAR_BODEGA", "ANALISTA_COMPRAS", "ADMIN_AUDITOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/alertas-stock/**").hasAnyRole("AUXILIAR_BODEGA", "ADMIN_AUDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/movimientos-inventario/**").hasAnyRole("AUXILIAR_BODEGA", "ANALISTA_COMPRAS", "ADMIN_AUDITOR")
                        .requestMatchers("/api/v1/movimientos-inventario/**").hasAnyRole("AUXILIAR_BODEGA", "ADMIN_AUDITOR")
                        .requestMatchers("/api/v1/documentos/**").hasAnyRole("AUXILIAR_BODEGA", "ANALISTA_COMPRAS", "ADMIN_AUDITOR")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        configuration.setExposedHeaders(List.of(
                "Content-Disposition"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
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
}
