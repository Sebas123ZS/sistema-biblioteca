package com.biblioteca.service;

import com.biblioteca.dto.DevolucionRequest;
import com.biblioteca.dto.DevolucionResponse;
import com.biblioteca.dto.PageResponse;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.ReglaNegocioException;
import com.biblioteca.model.Devolucion;
import com.biblioteca.model.Libro;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.Reserva;
import com.biblioteca.model.enums.EstadoLibroDevuelto;
import com.biblioteca.model.enums.EstadoPrestamo;
import com.biblioteca.repository.DevolucionRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.security.SecurityUtils;
import com.biblioteca.security.UsuarioPrincipal;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class DevolucionService {

    private final DevolucionRepository devolucionRepository;
    private final PrestamoRepository prestamoRepository;
    private final ReservaService reservaService;
    private final SecurityUtils securityUtils;

    public DevolucionService(DevolucionRepository devolucionRepository, PrestamoRepository prestamoRepository,
                             ReservaService reservaService, SecurityUtils securityUtils) {
        this.devolucionRepository = devolucionRepository;
        this.prestamoRepository = prestamoRepository;
        this.reservaService = reservaService;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public PageResponse<DevolucionResponse> buscar(String q, Pageable pageable) {
        UsuarioPrincipal actual = securityUtils.usuarioActual();
        Long filtroUsuario = actual.esBibliotecario() ? null : actual.getId();
        String filtro = StringUtils.hasText(q) ? q.trim() : null;
        return PageResponse.of(devolucionRepository.buscar(filtro, filtroUsuario, pageable), DevolucionResponse::from);
    }

    @Transactional(readOnly = true)
    public DevolucionResponse obtener(Long id) {
        Devolucion d = devolucionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Devolución", id));
        securityUtils.verificarPropietario(d.getPrestamo().getUsuario().getId());
        return DevolucionResponse.from(d);
    }

    /**
     * RF-10 / CU-10: registrar la devolución de un préstamo.
     * 1) Cierra el préstamo y calcula los días de retraso.
     * 2) Si el libro vuelve en buen estado, la unidad se entrega a la primera
     *    reserva en espera (RF-12) o vuelve a estar disponible.
     * 3) Si vuelve dañado, la unidad se retira del inventario.
     * Todo ocurre en una sola transacción.
     */
    @Transactional
    public DevolucionResponse registrar(DevolucionRequest req) {
        Prestamo p = prestamoRepository.findById(req.prestamoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Préstamo", req.prestamoId()));
        if (p.getEstado() == EstadoPrestamo.DEVUELTO || devolucionRepository.existsByPrestamoId(p.getId())) {
            throw new ReglaNegocioException("Este préstamo ya fue devuelto");
        }

        LocalDate hoy = LocalDate.now();
        long retraso = Math.max(0, ChronoUnit.DAYS.between(p.getFechaVencimiento(), hoy));
        EstadoLibroDevuelto estadoLibro = req.estadoLibro() != null ? req.estadoLibro() : EstadoLibroDevuelto.BUENO;

        Devolucion d = new Devolucion();
        d.setPrestamo(p);
        d.setFechaDevolucion(hoy);
        d.setDiasRetraso((int) retraso);
        d.setEstadoLibro(estadoLibro);
        d.setObservaciones(req.observaciones());
        p.setEstado(EstadoPrestamo.DEVUELTO);
        p.setDevolucion(d);

        Libro libro = p.getLibro();
        String asignadaA = null;
        if (estadoLibro == EstadoLibroDevuelto.DANADO) {
            libro.setCantidadTotal(Math.max(0, libro.getCantidadTotal() - 1));
        } else {
            Optional<Reserva> reserva = reservaService.liberarUnidad(libro);
            if (reserva.isPresent()) {
                asignadaA = reserva.get().getUsuario().getNombreCompleto();
            }
        }
        return DevolucionResponse.from(devolucionRepository.save(d), asignadaA);
    }
}
