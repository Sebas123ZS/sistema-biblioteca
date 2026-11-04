package com.biblioteca.repository;

import com.biblioteca.model.Usuario;
import com.biblioteca.model.enums.EstadoUsuario;
import com.biblioteca.model.enums.Rol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    /** Permite iniciar sesión con el usuario o con el correo. */
    @Query("SELECT u FROM Usuario u WHERE LOWER(u.username) = LOWER(:login) OR LOWER(u.correo) = LOWER(:login)")
    Optional<Usuario> buscarPorLogin(@Param("login") String login);

    boolean existsByDocumento(String documento);
    boolean existsByDocumentoAndIdNot(String documento, Long id);
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByCorreoIgnoreCaseAndIdNot(String correo, Long id);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    @Query("SELECT u FROM Usuario u WHERE " +
           "(:q IS NULL OR LOWER(CONCAT(u.nombres, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "  OR u.documento LIKE CONCAT('%', :q, '%') OR LOWER(u.correo) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:estado IS NULL OR u.estado = :estado) " +
           "AND (:rol IS NULL OR u.rol = :rol)")
    Page<Usuario> buscar(@Param("q") String q,
                         @Param("estado") EstadoUsuario estado,
                         @Param("rol") Rol rol,
                         Pageable pageable);

    long countByFechaRegistroAfter(LocalDateTime fecha);
}
