package com.farmared.opsintelligence.repository;

import com.farmared.opsintelligence.entity.UsuarioRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Long> {

    List<UsuarioRol> findByUsuarioId(Long usuarioId);

    Optional<UsuarioRol> findByUsuarioIdAndRolId(Long usuarioId, Long rolId);

    boolean existsByUsuarioIdAndRolId(Long usuarioId, Long rolId);

    void deleteByUsuarioId(Long usuarioId);

    @Query("""
            select distinct r.nombre
            from UsuarioRol ur
            join ur.rol r
            where ur.usuario.id = :usuarioId
              and r.nombre is not null
            order by r.nombre
            """)
    List<String> findRoleNamesByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("""
            select count(ur)
            from UsuarioRol ur
            where ur.rol.nombre in :rolNombres
              and ur.usuario.activo = true
            """)
    long countUsuariosActivosByRolNombreIn(@Param("rolNombres") List<String> rolNombres);
}
