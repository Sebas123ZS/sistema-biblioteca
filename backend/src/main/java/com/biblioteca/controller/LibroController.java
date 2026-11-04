package com.biblioteca.controller;

import com.biblioteca.dto.*;
import com.biblioteca.service.LibroService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroService libroService;

    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }

    /** RF-02 / RF-03: GET /api/libros?q=&categoriaId=&autorId=&disponible=true&page=0&size=10 */
    @GetMapping
    public PageResponse<LibroResponse> buscar(@RequestParam(name = "q", required = false) String q,
                                              @RequestParam(name = "categoriaId", required = false) Long categoriaId,
                                              @RequestParam(name = "autorId", required = false) Long autorId,
                                              @RequestParam(name = "disponible", required = false) Boolean disponible,
                                              @PageableDefault(size = 10, sort = "titulo", direction = Sort.Direction.ASC)
                                              Pageable pageable) {
        return libroService.buscar(q, categoriaId, autorId, disponible, pageable);
    }

    @GetMapping("/{id}")
    public LibroResponse obtener(@PathVariable("id") Long id) {
        return libroService.obtener(id);
    }

    /** RF-03: disponibilidad en tiempo real. */
    @GetMapping("/{id}/disponibilidad")
    public DisponibilidadResponse disponibilidad(@PathVariable("id") Long id) {
        return libroService.disponibilidad(id);
    }

    /** RF-01 */
    @PostMapping
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<LibroResponse> crear(@Valid @RequestBody LibroRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libroService.crear(req));
    }

    /** RF-02 */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public LibroResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody LibroRequest req) {
        return libroService.actualizar(id, req);
    }

    /** RF-05: asociar uno o varios autores. */
    @PutMapping("/{id}/autores")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public LibroResponse asignarAutores(@PathVariable("id") Long id, @Valid @RequestBody AsignarAutoresRequest req) {
        return libroService.asignarAutores(id, req.autorIds());
    }

    /** RF-02: baja lógica. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<Void> darDeBaja(@PathVariable("id") Long id) {
        libroService.darDeBaja(id);
        return ResponseEntity.noContent().build();
    }
}
