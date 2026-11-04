package com.biblioteca.controller;

import com.biblioteca.dto.CategoriaRequest;
import com.biblioteca.dto.CategoriaResponse;
import com.biblioteca.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaResponse> listar() {
        return categoriaService.listar();
    }

    @PostMapping
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crear(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public CategoriaResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody CategoriaRequest req) {
        return categoriaService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
