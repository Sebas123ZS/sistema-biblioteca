package com.biblioteca.controller;

import com.biblioteca.dto.PageResponse;
import com.biblioteca.dto.ReservaRequest;
import com.biblioteca.dto.ReservaResponse;
import com.biblioteca.model.enums.EstadoReserva;
import com.biblioteca.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    /** GET /api/reservas?estado=PENDIENTE&usuarioId=&libroId=&q= (el LECTOR solo ve las suyas). */
    @GetMapping
    public PageResponse<ReservaResponse> buscar(@RequestParam(name = "estado", required = false) EstadoReserva estado,
                                                @RequestParam(name = "usuarioId", required = false) Long usuarioId,
                                                @RequestParam(name = "libroId", required = false) Long libroId,
                                                @RequestParam(name = "q", required = false) String q,
                                                @PageableDefault(size = 10, sort = "fechaReserva", direction = Sort.Direction.DESC)
                                                Pageable pageable) {
        return reservaService.buscar(estado, usuarioId, libroId, q, pageable);
    }

    @GetMapping("/{id}")
    public ReservaResponse obtener(@PathVariable("id") Long id) {
        return reservaService.obtener(id);
    }

    /** RF-11: LECTOR y BIBLIOTECARIO pueden reservar. */
    @PostMapping
    public ResponseEntity<ReservaResponse> registrar(@Valid @RequestBody ReservaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.registrar(req));
    }

    /** RF-15: cancelar (el LECTOR solo las suyas). */
    @PatchMapping("/{id}/cancelar")
    public ReservaResponse cancelar(@PathVariable("id") Long id) {
        return reservaService.cancelar(id);
    }
}
