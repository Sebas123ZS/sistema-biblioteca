package com.biblioteca.repository;

import com.biblioteca.model.Devolucion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {

    boolean existsByPrestamoId(Long prestamoId);

    @Query(value = "SELECT d FROM Devolucion d JOIN FETCH d.prestamo p JOIN FETCH p.usuario u JOIN FETCH p.libro l " +
           "WHERE (:q IS NULL OR LOWER(CONCAT(u.nombres, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "       OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:usuarioId IS NULL OR u.id = :usuarioId)",
           countQuery = "SELECT COUNT(d) FROM Devolucion d JOIN d.prestamo p JOIN p.usuario u JOIN p.libro l " +
           "WHERE (:q IS NULL OR LOWER(CONCAT(u.nombres, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "       OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:usuarioId IS NULL OR u.id = :usuarioId)")
    Page<Devolucion> buscar(@Param("q") String q, @Param("usuarioId") Long usuarioId, Pageable pageable);

    long countByFechaDevolucion(LocalDate fecha);

    long countByFechaDevolucionGreaterThanEqual(LocalDate desde);
}
