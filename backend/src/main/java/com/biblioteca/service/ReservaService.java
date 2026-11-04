package com.biblioteca.service;

import com.biblioteca.dto.PageResponse;
import com.biblioteca.dto.ReservaRequest;
import com.biblioteca.dto.ReservaResponse;
import com.biblioteca.event.UsuarioDeshabilitadoEvent;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.ReglaNegocioException;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Reserva;
import com.biblioteca.model.Usuario;
import com.biblioteca.model.enums.EstadoPrestamo;
import com.biblioteca.model.enums.EstadoReserva;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.ReservaRepository;
import com.biblioteca.security.SecurityUtils;
import com.biblioteca.security.UsuarioPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    private static final Logger log = LoggerFactory.getLogger(ReservaService.class);
    static final List<EstadoReserva> RESERVAS_ABIERTAS = List.of(EstadoReserva.PENDIENTE, EstadoReserva.DISPONIBLE);

    private final ReservaRepository reservaRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final UsuarioService usuarioService;
    private final SecurityUtils securityUtils;
    private final int diasRetiro;

    public ReservaService(ReservaRepository reservaRepository, LibroRepository libroRepository,
                          PrestamoRepository prestamoRepository, UsuarioService usuarioService,
                          SecurityUtils securityUtils,
                          @Value("${biblioteca.reserva.dias-retiro:3}") int diasRetiro) {
        this.reservaRepository = reservaRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.usuarioService = usuarioService;
        this.securityUtils = securityUtils;
        this.diasRetiro = diasRetiro;
    }

    /** Listado. Un LECTOR solo ve sus propias reservas. */
    @Transactional(readOnly = true)
    public PageResponse<ReservaResponse> buscar(EstadoReserva estado, Long usuarioId, Long libroId, String q,
                                                Pageable pageable) {
        UsuarioPrincipal actual = securityUtils.usuarioActual();
        Long filtroUsuario = actual.esBibliotecario() ? usuarioId : actual.getId();
        String filtro = StringUtils.hasText(q) ? q.trim() : null;
        return PageResponse.of(reservaRepository.buscar(estado, filtroUsuario, libroId, filtro, pageable),
                ReservaResponse::from);
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtener(Long id) {
        Reserva r = buscarEntidad(id);
        securityUtils.verificarPropietario(r.getUsuario().getId());
        return ReservaResponse.from(r);
    }

    /** RF-11 / CU-11: registrar una reserva sobre un libro sin ejemplares disponibles. */
    @Transactional
    public ReservaResponse registrar(ReservaRequest req) {
        UsuarioPrincipal actual = securityUtils.usuarioActual();
        Long usuarioId;
        if (actual.esBibliotecario()) {
            if (req.usuarioId() == null) {
                throw new ReglaNegocioException("Seleccione el usuario para quien es la reserva");
            }
            usuarioId = req.usuarioId();
        } else {
            usuarioId = actual.getId();
        }

        Usuario usuario = usuarioService.buscarEntidad(usuarioId);
        usuarioService.validarHabilitado(usuario);

        Libro libro = libroRepository.findById(req.libroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro", req.libroId()));
        if (!libro.isActivo()) {
            throw new ReglaNegocioException("El libro fue dado de baja del catálogo");
        }
        if (libro.getCantidadDisponible() > 0) {
            throw new ReglaNegocioException("El libro tiene ejemplares disponibles. Solicite el préstamo directamente");
        }
        if (prestamoRepository.existsByUsuarioIdAndLibroIdAndEstadoIn(usuarioId, libro.getId(),
                List.of(EstadoPrestamo.ACTIVO, EstadoPrestamo.VENCIDO))) {
            throw new ReglaNegocioException("El usuario ya tiene este libro en préstamo");
        }
        if (reservaRepository.existsByUsuarioIdAndLibroIdAndEstadoIn(usuarioId, libro.getId(), RESERVAS_ABIERTAS)) {
            throw new ReglaNegocioException("Ya cuenta con una reserva activa para este libro");
        }

        Reserva r = new Reserva();
        r.setUsuario(usuario);
        r.setLibro(libro);
        r.setEstado(EstadoReserva.PENDIENTE);
        return ReservaResponse.from(reservaRepository.save(r));
    }

    /** RF-15: cancelar una reserva pendiente o disponible. */
    @Transactional
    public ReservaResponse cancelar(Long id) {
        Reserva r = buscarEntidad(id);
        securityUtils.verificarPropietario(r.getUsuario().getId());
        if (!RESERVAS_ABIERTAS.contains(r.getEstado())) {
            throw new ReglaNegocioException("Solo se pueden cancelar reservas pendientes o disponibles");
        }
        boolean teniaUnidadApartada = r.getEstado() == EstadoReserva.DISPONIBLE;
        r.setEstado(EstadoReserva.CANCELADA);
        if (teniaUnidadApartada) {
            liberarUnidad(r.getLibro());
        }
        return ReservaResponse.from(r);
    }

    /**
     * RF-12 / CU-12: una unidad del libro quedó libre (devolución, cancelación,
     * expiración o aumento de stock). Si hay reservas en espera se aparta para la
     * más antigua; si no, vuelve al stock disponible.
     *
     * @return la reserva que recibió la unidad, si hubo alguna.
     */
    @Transactional
    public Optional<Reserva> liberarUnidad(Libro libro) {
        Optional<Reserva> siguiente =
                reservaRepository.findFirstByLibroIdAndEstadoOrderByFechaReservaAsc(libro.getId(), EstadoReserva.PENDIENTE);
        if (siguiente.isPresent()) {
            Reserva r = siguiente.get();
            r.setEstado(EstadoReserva.DISPONIBLE);
            r.setFechaDisponible(LocalDate.now());
            r.setFechaExpiracion(LocalDate.now().plusDays(diasRetiro));
            log.info("Reserva {} pasó a DISPONIBLE para el usuario {}", r.getId(), r.getUsuario().getUsername());
        } else {
            libro.setCantidadDisponible(libro.getCantidadDisponible() + 1);
        }
        return siguiente;
    }

    /** Reparte las unidades libres de un libro entre las reservas en espera (ej. al aumentar stock). */
    @Transactional
    public void repartirDisponibles(Libro libro) {
        while (libro.getCantidadDisponible() > 0) {
            Optional<Reserva> siguiente = reservaRepository
                    .findFirstByLibroIdAndEstadoOrderByFechaReservaAsc(libro.getId(), EstadoReserva.PENDIENTE);
            if (siguiente.isEmpty()) {
                return;
            }
            libro.setCantidadDisponible(libro.getCantidadDisponible() - 1);
            liberarUnidad(libro);
        }
    }

    /** RN-08: reservas DISPONIBLE no retiradas a tiempo pasan a EXPIRADA y liberan su unidad. */
    @Transactional
    public int procesarExpiradas() {
        List<Reserva> vencidas =
                reservaRepository.findByEstadoAndFechaExpiracionBefore(EstadoReserva.DISPONIBLE, LocalDate.now());
        for (Reserva r : vencidas) {
            r.setEstado(EstadoReserva.EXPIRADA);
            liberarUnidad(r.getLibro());
        }
        return vencidas.size();
    }

    /**
     * RN-13: cuando un usuario es suspendido o desactivado se cancelan sus reservas abiertas.
     * Si alguna tenía una unidad apartada (DISPONIBLE), esa unidad pasa a la siguiente
     * reserva en espera o vuelve al stock. Se ejecuta dentro de la transacción de
     * UsuarioService.cambiarEstado (el evento es síncrono).
     */
    @EventListener
    @Transactional
    public void alDeshabilitarUsuario(UsuarioDeshabilitadoEvent evento) {
        List<Reserva> abiertas = reservaRepository.findByUsuarioIdAndEstadoIn(evento.usuarioId(), RESERVAS_ABIERTAS);
        List<Libro> unidadesPorLiberar = new ArrayList<>();
        for (Reserva r : abiertas) {
            if (r.getEstado() == EstadoReserva.DISPONIBLE) {
                unidadesPorLiberar.add(r.getLibro());
            }
            r.setEstado(EstadoReserva.CANCELADA);
        }
        unidadesPorLiberar.forEach(this::liberarUnidad);
        if (!abiertas.isEmpty()) {
            log.info("Usuario {} deshabilitado: {} reservas canceladas", evento.usuarioId(), abiertas.size());
        }
    }

    /** Al dar de baja un libro se cancelan sus reservas abiertas. */
    @Transactional
    public void cancelarReservasDeLibro(Long libroId) {
        reservaRepository.buscar(null, null, libroId, null, Pageable.unpaged()).stream()
                .filter(r -> RESERVAS_ABIERTAS.contains(r.getEstado()))
                .forEach(r -> r.setEstado(EstadoReserva.CANCELADA));
    }

    private Reserva buscarEntidad(Long id) {
        return reservaRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Reserva", id));
    }
}
