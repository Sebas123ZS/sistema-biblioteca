package com.biblioteca.service;

import com.biblioteca.dto.DisponibilidadResponse;
import com.biblioteca.dto.LibroRequest;
import com.biblioteca.dto.LibroResponse;
import com.biblioteca.dto.PageResponse;
import com.biblioteca.exception.RecursoNoEncontradoException;
import com.biblioteca.exception.ReglaNegocioException;
import com.biblioteca.model.Libro;
import com.biblioteca.model.enums.EstadoPrestamo;
import com.biblioteca.model.enums.EstadoReserva;
import com.biblioteca.repository.LibroRepository;
import com.biblioteca.repository.PrestamoRepository;
import com.biblioteca.repository.ReservaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;
    private final ReservaRepository reservaRepository;
    private final CategoriaService categoriaService;
    private final AutorService autorService;
    private final ReservaService reservaService;

    public LibroService(LibroRepository libroRepository, PrestamoRepository prestamoRepository,
                        ReservaRepository reservaRepository, CategoriaService categoriaService,
                        AutorService autorService, ReservaService reservaService) {
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
        this.reservaRepository = reservaRepository;
        this.categoriaService = categoriaService;
        this.autorService = autorService;
        this.reservaService = reservaService;
    }

    /** RF-02 / RF-03: buscar en el catálogo por título, ISBN, autor, categoría y disponibilidad. */
    @Transactional(readOnly = true)
    public PageResponse<LibroResponse> buscar(String q, Long categoriaId, Long autorId, Boolean disponible,
                                              Pageable pageable) {
        String filtro = StringUtils.hasText(q) ? q.trim() : null;
        int min = Boolean.TRUE.equals(disponible) ? 1 : 0;
        int max = Boolean.FALSE.equals(disponible) ? 0 : Integer.MAX_VALUE;
        return PageResponse.of(libroRepository.buscar(filtro, categoriaId, autorId, min, max, pageable),
                LibroResponse::from);
    }

    @Transactional(readOnly = true)
    public LibroResponse obtener(Long id) {
        return LibroResponse.from(buscarEntidad(id));
    }

    /** RF-03: disponibilidad en tiempo real de un libro. */
    @Transactional(readOnly = true)
    public DisponibilidadResponse disponibilidad(Long id) {
        Libro l = buscarEntidad(id);
        long enEspera = reservaRepository.countByLibroIdAndEstadoIn(id, List.of(EstadoReserva.PENDIENTE));
        return new DisponibilidadResponse(l.getId(), l.getTitulo(), l.getCantidadTotal(), l.getCantidadDisponible(),
                l.getCantidadTotal() - l.getCantidadDisponible(), l.estaDisponible(), enEspera);
    }

    /** RF-01 / CU-01: registrar libro. Todas sus unidades empiezan disponibles. */
    @Transactional
    public LibroResponse crear(LibroRequest req) {
        String isbn = normalizarIsbn(req.isbn());
        if (libroRepository.existsByIsbn(isbn)) {
            throw new ReglaNegocioException("Ya existe un libro con el ISBN " + isbn);
        }
        Libro l = new Libro();
        copiar(req, l, isbn);
        l.setCantidadTotal(req.cantidadTotal());
        l.setCantidadDisponible(req.cantidadTotal());
        l.setActivo(true);
        return LibroResponse.from(libroRepository.save(l));
    }

    /**
     * RF-02: actualizar. Si cambia la cantidad de ejemplares se respeta lo que está
     * prestado o apartado; las unidades nuevas se entregan primero a las reservas en espera.
     */
    @Transactional
    public LibroResponse actualizar(Long id, LibroRequest req) {
        Libro l = buscarEntidad(id);
        String isbn = normalizarIsbn(req.isbn());
        if (libroRepository.existsByIsbnAndIdNot(isbn, id)) {
            throw new ReglaNegocioException("Ya existe otro libro con el ISBN " + isbn);
        }
        int enUso = l.getCantidadTotal() - l.getCantidadDisponible();
        if (req.cantidadTotal() < enUso) {
            throw new ReglaNegocioException("No puede haber menos de " + enUso
                    + " ejemplares: esa cantidad está prestada o apartada por reservas");
        }
        copiar(req, l, isbn);
        l.setCantidadTotal(req.cantidadTotal());
        l.setCantidadDisponible(req.cantidadTotal() - enUso);
        reservaService.repartirDisponibles(l);
        return LibroResponse.from(l);
    }

    /** RF-05 / CU-05: asociar uno o varios autores al libro. */
    @Transactional
    public LibroResponse asignarAutores(Long id, Set<Long> autorIds) {
        Libro l = buscarEntidad(id);
        l.getAutores().clear();
        l.getAutores().addAll(autorService.buscarVarios(autorIds));
        return LibroResponse.from(l);
    }

    /** RF-02: baja lógica. No se permite si hay ejemplares sin devolver. */
    @Transactional
    public void darDeBaja(Long id) {
        Libro l = buscarEntidad(id);
        if (prestamoRepository.existsByLibroIdAndEstadoIn(id, List.of(EstadoPrestamo.ACTIVO, EstadoPrestamo.VENCIDO))) {
            throw new ReglaNegocioException("No se puede dar de baja: hay ejemplares de este libro en préstamo");
        }
        reservaService.cancelarReservasDeLibro(id);
        l.setActivo(false);
    }

    Libro buscarEntidad(Long id) {
        Libro l = libroRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Libro", id));
        if (!l.isActivo()) {
            throw new RecursoNoEncontradoException("El libro con id " + id + " fue dado de baja");
        }
        return l;
    }

    private void copiar(LibroRequest req, Libro l, String isbn) {
        l.setIsbn(isbn);
        l.setTitulo(req.titulo().trim());
        l.setEditorial(req.editorial());
        l.setAnioPublicacion(req.anioPublicacion());
        l.setCategoria(categoriaService.buscarEntidad(req.categoriaId()));
        l.getAutores().clear();
        l.getAutores().addAll(autorService.buscarVarios(req.autorIds()));
        l.setPortadaUrl(StringUtils.hasText(req.portadaUrl()) ? req.portadaUrl().trim() : null);
    }

    private String normalizarIsbn(String isbn) {
        return isbn.trim().toUpperCase();
    }
}
