package com.biblioteca.controller;

import com.biblioteca.dto.DevolucionRequest;
import com.biblioteca.dto.DevolucionResponse;
import com.biblioteca.dto.PageResponse;
import com.biblioteca.service.DevolucionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devoluciones")
public class DevolucionController {

    private final DevolucionService devolucionService;

    public DevolucionController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    /** RF-18: historial de devoluciones. */
    @GetMapping
    public PageResponse<DevolucionResponse> buscar(@RequestParam(name = "q", required = false) String q,
                                                   @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
                                                   Pageable pageable) {
        return devolucionService.buscar(q, pageable);
    }

    @GetMapping("/{id}")
    public DevolucionResponse obtener(@PathVariable("id") Long id) {
        return devolucionService.obtener(id);
    }

    /** RF-10 */
    @PostMapping
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<DevolucionResponse> registrar(@Valid @RequestBody DevolucionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(devolucionService.registrar(req));
    }
}
