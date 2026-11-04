package com.biblioteca.controller;

import com.biblioteca.dto.AutorRequest;
import com.biblioteca.dto.AutorResponse;
import com.biblioteca.dto.PageResponse;
import com.biblioteca.service.AutorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/autores")
public class AutorController {

    private final AutorService autorService;

    public AutorController(AutorService autorService) {
        this.autorService = autorService;
    }

    /** RF-04: GET /api/autores?q=garcia&page=0&size=10 */
    @GetMapping
    public PageResponse<AutorResponse> buscar(@RequestParam(name = "q", required = false) String q,
                                              @PageableDefault(size = 10, sort = "apellidos", direction = Sort.Direction.ASC)
                                              Pageable pageable) {
        return autorService.buscar(q, pageable);
    }

    @GetMapping("/{id}")
    public AutorResponse obtener(@PathVariable("id") Long id) {
        return autorService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<AutorResponse> crear(@Valid @RequestBody AutorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(autorService.crear(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public AutorResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody AutorRequest req) {
        return autorService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        autorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
