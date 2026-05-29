package com.farmared.opsintelligence.service.impl;

import com.farmared.opsintelligence.dto.request.UsuarioEstadoUpdateRequest;
import com.farmared.opsintelligence.dto.request.UsuarioRolUpdateRequest;
import com.farmared.opsintelligence.dto.response.UsuarioResponse;
import com.farmared.opsintelligence.entity.Rol;
import com.farmared.opsintelligence.entity.Usuario;
import com.farmared.opsintelligence.entity.UsuarioRol;
import com.farmared.opsintelligence.exception.BusinessRuleException;
import com.farmared.opsintelligence.exception.ResourceNotFoundException;
import com.farmared.opsintelligence.mapper.UsuarioMapper;
import com.farmared.opsintelligence.repository.RolRepository;
import com.farmared.opsintelligence.repository.UsuarioRepository;
import com.farmared.opsintelligence.repository.UsuarioRolRepository;
import com.farmared.opsintelligence.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private static final String ADMIN_ROLE = "ROLE_ADMIN_AUDITOR";
    private static final List<String> ADMIN_ROLE_NAMES = List.of("ROLE_ADMIN_AUDITOR", "ADMIN_AUDITOR");

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse consultarPorId(Long id) {
        return toResponse(buscarUsuario(id));
    }

    @Override
    public UsuarioResponse actualizarRoles(Long id, UsuarioRolUpdateRequest request) {
        Usuario usuario = buscarUsuario(id);
        Set<String> rolesSolicitados = normalizarRoles(request.roles());

        if (rolesSolicitados.isEmpty()) {
            throw new BusinessRuleException("El usuario debe tener al menos un rol");
        }

        List<String> rolesActuales = usuarioRolRepository.findRoleNamesByUsuarioId(usuario.getId());
        boolean tieneAdminActualmente = contieneRol(rolesActuales, ADMIN_ROLE);
        boolean tendraAdmin = rolesSolicitados.contains(ADMIN_ROLE);

        if (tieneAdminActualmente && !tendraAdmin && esUltimoAdminActivo(usuario)) {
            throw new BusinessRuleException("No se puede quitar ROLE_ADMIN_AUDITOR al ultimo admin activo");
        }

        List<Rol> rolesValidados = rolesSolicitados
                .stream()
                .map(this::buscarRolPorNombre)
                .toList();

        usuarioRolRepository.deleteByUsuarioId(usuario.getId());
        usuarioRolRepository.flush();

        List<UsuarioRol> nuevosRoles = rolesValidados
                .stream()
                .map(rol -> {
                    UsuarioRol usuarioRol = new UsuarioRol();
                    usuarioRol.setUsuario(usuario);
                    usuarioRol.setRol(rol);
                    return usuarioRol;
                })
                .toList();

        usuarioRolRepository.saveAll(nuevosRoles);

        return toResponse(usuario);
    }

    @Override
    public UsuarioResponse actualizarEstado(Long id, UsuarioEstadoUpdateRequest request) {
        Usuario usuario = buscarUsuario(id);

        if (Boolean.FALSE.equals(request.activo())
                && Boolean.TRUE.equals(usuario.getActivo())
                && usuarioTieneRol(usuario.getId(), ADMIN_ROLE)
                && esUltimoAdminActivo(usuario)) {
            throw new BusinessRuleException("No se puede desactivar al ultimo usuario con ROLE_ADMIN_AUDITOR");
        }

        usuario.setActivo(request.activo());
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        return toResponse(usuarioActualizado);
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    private Rol buscarRolPorNombre(String nombreRol) {
        return rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + nombreRol));
    }

    private Set<String> normalizarRoles(List<String> roles) {
        Set<String> rolesNormalizados = new LinkedHashSet<>();

        if (roles == null) {
            return rolesNormalizados;
        }

        for (String rol : roles) {
            if (rol == null || rol.isBlank()) {
                continue;
            }

            rolesNormalizados.add(normalizarRol(rol));
        }

        return rolesNormalizados;
    }

    private String normalizarRol(String rol) {
        String limpio = rol.trim();
        return limpio.startsWith("ROLE_") ? limpio : "ROLE_" + limpio;
    }

    private boolean usuarioTieneRol(Long usuarioId, String nombreRol) {
        return usuarioRolRepository.findRoleNamesByUsuarioId(usuarioId)
                .stream()
                .map(this::normalizarRol)
                .anyMatch(nombreRol::equals);
    }

    private boolean contieneRol(List<String> rolesUsuario, String nombreRol) {
        return rolesUsuario
                .stream()
                .map(this::normalizarRol)
                .anyMatch(nombreRol::equals);
    }

    private boolean esUltimoAdminActivo(Usuario usuario) {
        return Boolean.TRUE.equals(usuario.getActivo())
                && usuarioRolRepository.countUsuariosActivosByRolNombreIn(ADMIN_ROLE_NAMES) <= 1;
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return usuarioMapper.toResponse(
                usuario,
                usuarioRolRepository.findRoleNamesByUsuarioId(usuario.getId())
        );
    }
}
