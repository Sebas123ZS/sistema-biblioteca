package com.biblioteca.repository;

import com.biblioteca.model.Prestamo;
import com.biblioteca.model.enums.EstadoPrestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    @Query(value = "SELECT p FROM Prestamo p JOIN FETCH p.usuario u JOIN FETCH p.libro l WHERE " +
           "p.estado IN :estados " +
           "AND (:usuarioId IS NULL OR u.id = :usuarioId) " +
           "AND (:q IS NULL OR LOWER(CONCAT(u.nombres, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "     OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :q, '%')) OR u.documento LIKE CONCAT('%', :q, '%'))",
           countQuery = "SELECT COUNT(p) FROM Prestamo p JOIN p.usuario u JOIN p.libro l WHERE " +
           "p.estado IN :estados " +
           "AND (:usuarioId IS NULL OR u.id = :usuarioId) " +
           "AND (:q IS NULL OR LOWER(CONCAT(u.nombres, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "     OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :q, '%')) OR u.documento LIKE CONCAT('%', :q, '%'))")
    Page<Prestamo> buscar(@Param("estados") Collection<EstadoPrestamo> estados,
                          @Param("usuarioId") Long usuarioId,
                          @Param("q") String q,
                          Pageable pageable);

    long countByUsuarioIdAndEstadoIn(Long usuarioId, Collection<EstadoPrestamo> estados);

    long countByUsuarioIdAndEstado(Long usuarioId, EstadoPrestamo estado);

    boolean existsByUsuarioIdAndLibroIdAndEstadoIn(Long usuarioId, Long libroId, Collection<EstadoPrestamo> estados);

    boolean existsByLibroIdAndEstadoIn(Long libroId, Collection<EstadoPrestamo> estados);

    long countByEstado(EstadoPrestamo estado);

    /** Marca como VENCIDO todo préstamo activo cuya fecha límite ya pasó (RN-09). */
    @Modifying
    @Query("UPDATE Prestamo p SET p.estado = com.biblioteca.model.enums.EstadoPrestamo.VENCIDO " +
           "WHERE p.estado = com.biblioteca.model.enums.EstadoPrestamo.ACTIVO AND p.fechaVencimiento < :hoy")
    int marcarVencidos(@Param("hoy") LocalDate hoy);
}
