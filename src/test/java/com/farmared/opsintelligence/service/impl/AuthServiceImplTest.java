package com.farmared.opsintelligence.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.farmared.opsintelligence.dto.request.AuthRequest;
import com.farmared.opsintelligence.dto.request.RegisterRequest;
import com.farmared.opsintelligence.dto.response.AuthResponse;
import com.farmared.opsintelligence.entity.Rol;
import com.farmared.opsintelligence.entity.Usuario;
import com.farmared.opsintelligence.entity.UsuarioRol;
import com.farmared.opsintelligence.exception.DuplicateResourceException;
import com.farmared.opsintelligence.repository.RolRepository;
import com.farmared.opsintelligence.repository.UsuarioRepository;
import com.farmared.opsintelligence.repository.UsuarioRolRepository;
import com.farmared.opsintelligence.security.JwtService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String DEFAULT_ROLE = "ROLE_AUXILIAR_BODEGA";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private UsuarioRolRepository usuarioRolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerCreaUsuarioConPasswordCifradaYRolPorDefecto() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "auxiliar.demo",
                "auxiliar.demo@farmared.local",
                "secret123",
                "Auxiliar Demo"
        );
        Rol rol = rol(DEFAULT_ROLE);
        UserDetails userDetails = userDetails("auxiliar.demo", DEFAULT_ROLE);

        when(usuarioRepository.existsByUsername(request.username())).thenReturn(false);
        when(usuarioRepository.existsByEmail(request.email())).thenReturn(false);
        when(rolRepository.findByNombre(DEFAULT_ROLE)).thenReturn(Optional.of(rol));
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-secret");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(10L);
            return usuario;
        });
        when(usuarioRolRepository.save(any(UsuarioRol.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userDetailsService.loadUserByUsername(request.username())).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");
        when(usuarioRolRepository.findRoleNamesByUsuarioId(10L)).thenReturn(List.of(DEFAULT_ROLE));

        // Act
        AuthResponse response = authService.register(request);

        // Assert
        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        ArgumentCaptor<UsuarioRol> usuarioRolCaptor = ArgumentCaptor.forClass(UsuarioRol.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());
        verify(usuarioRolRepository).save(usuarioRolCaptor.capture());

        Usuario usuarioGuardado = usuarioCaptor.getValue();
        UsuarioRol rolAsignado = usuarioRolCaptor.getValue();

        assertEquals("encoded-secret", usuarioGuardado.getPassword());
        assertTrue(usuarioGuardado.getActivo());
        assertEquals(DEFAULT_ROLE, rolAsignado.getRol().getNombre());
        assertEquals("jwt-token", response.token());
        assertEquals(List.of(DEFAULT_ROLE), response.roles());
    }

    @Test
    void registerLanzaDuplicateResourceExceptionSiUsernameYaExiste() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "admin",
                "admin@farmared.local",
                "admin123",
                "Admin FarmaRed"
        );
        when(usuarioRepository.existsByUsername(request.username())).thenReturn(true);

        // Act / Assert
        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void loginAutenticaUsuarioYRetornaToken() {
        // Arrange
        AuthRequest request = new AuthRequest("admin", "admin123");
        Usuario usuario = usuario(1L, "admin", "admin@farmared.local");
        UserDetails userDetails = userDetails("admin", "ROLE_ADMIN_AUDITOR");

        when(usuarioRepository.findByUsername(request.username())).thenReturn(Optional.of(usuario));
        when(userDetailsService.loadUserByUsername(usuario.getUsername())).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("admin-token");
        when(usuarioRolRepository.findRoleNamesByUsuarioId(1L)).thenReturn(List.of("ROLE_ADMIN_AUDITOR"));

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(authCaptor.capture());

        UsernamePasswordAuthenticationToken authToken = authCaptor.getValue();
        assertEquals("admin", authToken.getPrincipal());
        assertEquals("admin123", authToken.getCredentials());
        assertEquals("admin-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(List.of("ROLE_ADMIN_AUDITOR"), response.roles());
    }

    private static Rol rol(String nombre) {
        Rol rol = new Rol();
        rol.setId(2L);
        rol.setNombre(nombre);
        rol.setActivo(true);
        return rol;
    }

    private static Usuario usuario(Long id, String username, String email) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPassword("encoded");
        usuario.setNombreCompleto("Usuario Demo");
        usuario.setActivo(true);
        return usuario;
    }

    private static UserDetails userDetails(String username, String role) {
        return User.withUsername(username)
                .password("encoded")
                .authorities(role)
                .build();
    }
}
