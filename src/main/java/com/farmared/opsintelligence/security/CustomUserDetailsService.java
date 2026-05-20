package com.farmared.opsintelligence.security;

import com.farmared.opsintelligence.entity.Usuario;
import com.farmared.opsintelligence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .disabled(!usuario.getActivo())
                .authorities(
                        usuario.getRoles()
                                .stream()
                                .map(usuarioRol -> usuarioRol.getRol().getNombre())
                                .map(rol -> rol.startsWith("ROLE_") ? rol : "ROLE_" + rol)
                                .map(SimpleGrantedAuthority::new)
                                .toList()
                )
                .build();
    }
}