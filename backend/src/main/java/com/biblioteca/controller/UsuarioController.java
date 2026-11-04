package com.biblioteca.controller;

import com.biblioteca.dto.*;
import com.biblioteca.model.enums.EstadoUsuario;
import com.biblioteca.model.enums.Rol;
import com.biblioteca.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** RF-06: GET /api/usuarios?q=&estado=ACTIVO&rol=LECTOR */
    @GetMapping
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public PageResponse<UsuarioResponse> buscar(@RequestParam(name = "q", required = false) String q,
                                                @RequestParam(name = "estado", required = false) EstadoUsuario estado,
                                                @RequestParam(name = "rol", required = false) Rol rol,
                                                @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)
                                                Pageable pageable) {
        return usuarioService.buscar(q, estado, rol, pageable);
    }

    /** El LECTOR solo puede consultar su propio id (se valida en el servicio). */
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable("id") Long id) {
        return usuarioService.obtener(id);
    }

    @GetMapping("/{id}/resumen")
    public UsuarioResumenResponse resumen(@PathVariable("id") Long id) {
        return usuarioService.resumen(id);
    }

    /** RF-07: historial de préstamos de un usuario. */
    @GetMapping("/{id}/prestamos")
    public PageResponse<PrestamoResponse> historial(@PathVariable("id") Long id,
                                                    @PageableDefault(size = 10, sort = "fechaPrestamo", direction = Sort.Direction.DESC)
                                                    Pageable pageable) {
        return usuarioService.historial(id, pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public UsuarioResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody UsuarioRequest req) {
        return usuarioService.actualizar(id, req);
    }

    /** Activar / suspender / desactivar. */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public UsuarioResponse cambiarEstado(@PathVariable("id") Long id, @Valid @RequestBody CambiarEstadoUsuarioRequest req) {
        return usuarioService.cambiarEstado(id, req.estado());
    }

    /** Baja lógica (estado INACTIVO). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
