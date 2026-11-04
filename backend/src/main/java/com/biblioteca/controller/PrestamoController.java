package com.biblioteca.controller;

import com.biblioteca.dto.PageResponse;
import com.biblioteca.dto.PrestamoRequest;
import com.biblioteca.dto.PrestamoResponse;
import com.biblioteca.model.enums.EstadoPrestamo;
import com.biblioteca.service.PrestamoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    /**
     * RF-09: GET /api/prestamos?estado=ACTIVO|VENCIDO|DEVUELTO&usuarioId=&q=
     * Se pueden enviar varios estados separados por coma: estado=ACTIVO,VENCIDO
     */
    @GetMapping
    public PageResponse<PrestamoResponse> buscar(@RequestParam(name = "estado", required = false) List<EstadoPrestamo> estado,
                                                 @RequestParam(name = "usuarioId", required = false) Long usuarioId,
                                                 @RequestParam(name = "q", required = false) String q,
                                                 @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
                                                 Pageable pageable) {
        return prestamoService.buscar(estado, usuarioId, q, pageable);
    }

    @GetMapping("/{id}")
    public PrestamoResponse obtener(@PathVariable("id") Long id) {
        return prestamoService.obtener(id);
    }

    /** RF-08 */
    @PostMapping
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<PrestamoResponse> registrar(@Valid @RequestBody PrestamoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prestamoService.registrar(req));
    }
}
