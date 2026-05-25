package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.request.AuthRequest;
import com.farmared.opsintelligence.dto.request.RegisterRequest;
import com.farmared.opsintelligence.dto.response.AuthResponse;
import com.farmared.opsintelligence.entity.Rol;
import com.farmared.opsintelligence.entity.Usuario;
import com.farmared.opsintelligence.entity.UsuarioRol;
import com.farmared.opsintelligence.exception.DuplicateResourceException;
import com.farmared.opsintelligence.exception.ResourceNotFoundException;
import com.farmared.opsintelligence.repository.RolRepository;
import com.farmared.opsintelligence.repository.UsuarioRepository;
import com.farmared.opsintelligence.repository.UsuarioRolRepository;
import com.farmared.opsintelligence.security.JwtService;
import com.farmared.opsintelligence.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("El username ya está registrado");
        }

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("El email ya está registrado");
        }

        Rol rolUser = rolRepository.findByNombre("ROLE_AUXILIAR_BODEGA")
                .orElseGet(() -> {
                    Rol nuevoRol = new Rol();
                    nuevoRol.setNombre("ROLE_AUXILIAR_BODEGA");
                    nuevoRol.setDescripcion("Auxiliar de Bodega");
                    nuevoRol.setActivo(true);
                    return rolRepository.save(nuevoRol);
                });

        Usuario usuario = new Usuario();
        usuario.setUsername(request.username());
        usuario.setEmail(request.email());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setNombreCompleto(request.nombreCompleto());
        usuario.setActivo(true);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(usuarioGuardado);
        usuarioRol.setRol(rolUser);
        usuarioRolRepository.save(usuarioRol);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuarioGuardado.getUsername());
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                "Bearer",
                usuarioGuardado.getId(),
                usuarioGuardado.getUsername(),
                usuarioGuardado.getEmail()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getUsername());
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail()
        );
    }
}
