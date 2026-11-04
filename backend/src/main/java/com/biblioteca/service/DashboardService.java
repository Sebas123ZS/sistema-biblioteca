package com.biblioteca.service;

import com.biblioteca.dto.DashboardResponse;
import com.biblioteca.dto.LibroResponse;
import com.biblioteca.model.enums.EstadoPrestamo;
import com.biblioteca.model.enums.EstadoReserva;
import com.biblioteca.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;
    private final DevolucionRepository devolucionRepository;
    private final ReservaRepository reservaRepository;
    private final CategoriaRepository categoriaRepository;
    private final PrestamoService prestamoService;

    public DashboardService(LibroRepository libroRepository, UsuarioRepository usuarioRepository,
                            PrestamoRepository prestamoRepository, DevolucionRepository devolucionRepository,
                            ReservaRepository reservaRepository, CategoriaRepository categoriaRepository,
                            PrestamoService prestamoService) {
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
        this.prestamoRepository = prestamoRepository;
        this.devolucionRepository = devolucionRepository;
        this.reservaRepository = reservaRepository;
        this.categoriaRepository = categoriaRepository;
        this.prestamoService = prestamoService;
    }

    /** RF-16: indicadores generales para el panel del bibliotecario. */
    @Transactional
    public DashboardResponse resumen() {
        prestamoService.actualizarVencidos();
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        List<DashboardResponse.CategoriaConteo> categorias = categoriaRepository.contarLibrosPorCategoria().stream()
                .limit(8)
                .map(fila -> new DashboardResponse.CategoriaConteo((String) fila[0], ((Number) fila[1]).longValue()))
                .toList();
        List<LibroResponse> ultimos = libroRepository.findTop5ByActivoTrueOrderByFechaRegistroDesc().stream()
                .map(LibroResponse::from).toList();

        return new DashboardResponse(
                libroRepository.contarActivos(),
                libroRepository.sumarEjemplares(),
                libroRepository.sumarDisponibles(),
                usuarioRepository.count(),
                usuarioRepository.countByFechaRegistroAfter(inicioMes.atStartOfDay()),
                prestamoRepository.countByEstado(EstadoPrestamo.ACTIVO),
                prestamoRepository.countByEstado(EstadoPrestamo.VENCIDO),
                devolucionRepository.countByFechaDevolucion(hoy),
                devolucionRepository.countByFechaDevolucionGreaterThanEqual(inicioMes),
                reservaRepository.countByEstado(EstadoReserva.PENDIENTE),
                reservaRepository.countByEstado(EstadoReserva.DISPONIBLE),
                categorias,
                ultimos);
    }
}
