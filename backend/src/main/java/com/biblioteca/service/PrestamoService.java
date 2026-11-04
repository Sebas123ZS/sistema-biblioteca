package com.biblioteca.service;

import com.biblioteca.dto.PageResponse;
import com.biblioteca.dto.PrestamoRequest;
import com.biblioteca.dto.PrestamoResponse;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.ReglaNegocioException;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Reserva;
import com.biblioteca.model.Usuario;
import com.biblioteca.model.enums.EstadoPrestamo;
import com.biblioteca.model.enums.EstadoReserva;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.ReservaRepository;
import com.biblioteca.security.SecurityUtils;
import com.biblioteca.security.UsuarioPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PrestamoService {

    static final List<EstadoPrestamo> PRESTAMOS_ABIERTOS = List.of(EstadoPrestamo.ACTIVO, EstadoPrestamo.VENCIDO);
    private static final int MAX_DIAS_PRESTAMO = 60;

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final ReservaRepository reservaRepository;
    private final UsuarioService usuarioService;
    private final SecurityUtils securityUtils;
    private final int diasPrestamo;
    private final int maxActivos;

    public PrestamoService(PrestamoRepository prestamoRepository, LibroRepository libroRepository,
                           ReservaRepository reservaRepository, UsuarioService usuarioService,
                           SecurityUtils securityUtils,
                           @Value("${biblioteca.prestamo.dias:14}") int diasPrestamo,
                           @Value("${biblioteca.prestamo.max-activos:3}") int maxActivos) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.reservaRepository = reservaRepository;
        this.usuarioService = usuarioService;
        this.securityUtils = securityUtils;
        this.diasPrestamo = diasPrestamo;
        this.maxActivos = maxActivos;
    }

    /**
     * RF-09 / CU-09: listar préstamos (activos, vencidos o devueltos). Un LECTOR solo ve los suyos.
     * Acepta uno o varios estados (ej.: ACTIVO,VENCIDO = préstamos pendientes de devolución);
     * si no se indica ninguno, se listan todos.
     */
    @Transactional
    public PageResponse<PrestamoResponse> buscar(List<EstadoPrestamo> estados, Long usuarioId, String q,
                                                 Pageable pageable) {
        actualizarVencidos();
        UsuarioPrincipal actual = securityUtils.usuarioActual();
        Long filtroUsuario = actual.esBibliotecario() ? usuarioId : actual.getId();
        String filtro = StringUtils.hasText(q) ? q.trim() : null;
        return PageResponse.of(prestamoRepository.buscar(estadosOTodos(estados), filtroUsuario, filtro, pageable),
                PrestamoResponse::from);
    }

    /** Si la lista viene vacía se consideran todos los estados. */
    static List<EstadoPrestamo> estadosOTodos(List<EstadoPrestamo> estados) {
        if (estados == null) {
            return List.of(EstadoPrestamo.values());
        }
        List<EstadoPrestamo> sinNulos = estados.stream().filter(Objects::nonNull).distinct().toList();
        return sinNulos.isEmpty() ? List.of(EstadoPrestamo.values()) : sinNulos;
    }

    @Transactional(readOnly = true)
    public PrestamoResponse obtener(Long id) {
        Prestamo p = buscarEntidad(id);
        securityUtils.verificarPropietario(p.getUsuario().getId());
        return PrestamoResponse.from(p);
    }

    /** RF-08 / CU-08: registrar un préstamo aplicando las reglas RN-01 a RN-05. */
    @Transactional
    public PrestamoResponse registrar(PrestamoRequest req) {
        actualizarVencidos();

        Usuario usuario = usuarioService.buscarEntidad(req.usuarioId());
        usuarioService.validarHabilitado(usuario);                                   // RN-03

        long abiertos = prestamoRepository.countByUsuarioIdAndEstadoIn(usuario.getId(), PRESTAMOS_ABIERTOS);
        if (abiertos >= maxActivos) {                                                 // RN-02
            throw new ReglaNegocioException("El usuario ya tiene " + abiertos
                    + " préstamos activos (máximo " + maxActivos + ")");
        }

        Libro libro = libroRepository.findById(req.libroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro", req.libroId()));
        if (!libro.isActivo()) {
            throw new ReglaNegocioException("El libro fue dado de baja del catálogo");
        }
        if (prestamoRepository.existsByUsuarioIdAndLibroIdAndEstadoIn(usuario.getId(), libro.getId(),
                PRESTAMOS_ABIERTOS)) {                                                // RN-05
            throw new ReglaNegocioException("El usuario ya tiene este libro en préstamo");
        }

        // RN-04: si el usuario tiene una unidad apartada por reserva, se usa esa unidad.
        Optional<Reserva> apartada = reservaRepository
                .findFirstByUsuarioIdAndLibroIdAndEstado(usuario.getId(), libro.getId(), EstadoReserva.DISPONIBLE);
        Reserva reservaAtendida = null;
        if (apartada.isPresent()) {
            reservaAtendida = apartada.get();
        } else {
            if (libro.getCantidadDisponible() <= 0) {
                throw new ReglaNegocioException("No hay ejemplares disponibles de \"" + libro.getTitulo()
                        + "\". Puede registrar una reserva");
            }
            libro.setCantidadDisponible(libro.getCantidadDisponible() - 1);
            // Si además tenía una reserva pendiente de este libro, queda atendida.
            reservaAtendida = reservaRepository
                    .findFirstByUsuarioIdAndLibroIdAndEstado(usuario.getId(), libro.getId(), EstadoReserva.PENDIENTE)
                    .orElse(null);
        }
        if (reservaAtendida != null) {
            reservaAtendida.setEstado(EstadoReserva.ATENDIDA);
        }

        LocalDate hoy = LocalDate.now();
        LocalDate vencimiento = req.fechaVencimiento() != null ? req.fechaVencimiento() : hoy.plusDays(diasPrestamo);
        if (!vencimiento.isAfter(hoy)) {
            throw new ReglaNegocioException("La fecha de vencimiento debe ser posterior a hoy");
        }
        if (vencimiento.isAfter(hoy.plusDays(MAX_DIAS_PRESTAMO))) {
            throw new ReglaNegocioException("El plazo máximo de un préstamo es de " + MAX_DIAS_PRESTAMO + " días");
        }

        Prestamo p = new Prestamo();
        p.setUsuario(usuario);
        p.setLibro(libro);
        p.setFechaPrestamo(hoy);
        p.setFechaVencimiento(vencimiento);
        p.setEstado(EstadoPrestamo.ACTIVO);
        p.setReserva(reservaAtendida);
        return PrestamoResponse.from(prestamoRepository.save(p));
    }

    /** RN-09: los préstamos activos cuya fecha límite ya pasó se marcan como VENCIDO. */
    @Transactional
    public int actualizarVencidos() {
        return prestamoRepository.marcarVencidos(LocalDate.now());
    }

    Prestamo buscarEntidad(Long id) {
        return prestamoRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Préstamo", id));
    }
}
