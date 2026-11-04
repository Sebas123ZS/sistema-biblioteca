package com.biblioteca.repository;

import com.biblioteca.model.Reserva;
import com.biblioteca.model.enums.EstadoReserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query(value = "SELECT r FROM Reserva r JOIN FETCH r.usuario u JOIN FETCH r.libro l WHERE " +
           "(:estado IS NULL OR r.estado = :estado) " +
           "AND (:usuarioId IS NULL OR u.id = :usuarioId) " +
           "AND (:libroId IS NULL OR l.id = :libroId) " +
           "AND (:q IS NULL OR LOWER(CONCAT(u.nombres, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "     OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :q, '%')))",
           countQuery = "SELECT COUNT(r) FROM Reserva r JOIN r.usuario u JOIN r.libro l WHERE " +
           "(:estado IS NULL OR r.estado = :estado) " +
           "AND (:usuarioId IS NULL OR u.id = :usuarioId) " +
           "AND (:libroId IS NULL OR l.id = :libroId) " +
           "AND (:q IS NULL OR LOWER(CONCAT(u.nombres, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "     OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Reserva> buscar(@Param("estado") EstadoReserva estado,
                         @Param("usuarioId") Long usuarioId,
                         @Param("libroId") Long libroId,
                         @Param("q") String q,
                         Pageable pageable);

    boolean existsByUsuarioIdAndLibroIdAndEstadoIn(Long usuarioId, Long libroId, Collection<EstadoReserva> estados);

    /** Primera reserva pendiente de un libro según el orden de llegada (FIFO). */
    Optional<Reserva> findFirstByLibroIdAndEstadoOrderByFechaReservaAsc(Long libroId, EstadoReserva estado);

    Optional<Reserva> findFirstByUsuarioIdAndLibroIdAndEstado(Long usuarioId, Long libroId, EstadoReserva estado);

    List<Reserva> findByEstadoAndFechaExpiracionBefore(EstadoReserva estado, LocalDate fecha);

    List<Reserva> findByUsuarioIdAndEstadoIn(Long usuarioId, Collection<EstadoReserva> estados);

    long countByEstado(EstadoReserva estado);

    long countByUsuarioIdAndEstadoIn(Long usuarioId, Collection<EstadoReserva> estados);

    long countByLibroIdAndEstadoIn(Long libroId, Collection<EstadoReserva> estados);
}
