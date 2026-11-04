package com.biblioteca.dto;

import com.biblioteca.model.Reserva;
import com.biblioteca.model.enums.EstadoReserva;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservaResponse(
        Long id,
        String codigo,
        Long usuarioId,
        String usuarioNombre,
        String usuarioCorreo,
        Long libroId,
        String libroTitulo,
        String libroPortadaUrl,
        LocalDateTime fechaReserva,
        LocalDate fechaDisponible,
        LocalDate fechaExpiracion,
        EstadoReserva estado
) {
    public static ReservaResponse from(Reserva r) {
        return new ReservaResponse(r.getId(), String.format("RES-%04d", r.getId()),
                r.getUsuario().getId(), r.getUsuario().getNombreCompleto(), r.getUsuario().getCorreo(),
                r.getLibro().getId(), r.getLibro().getTitulo(), r.getLibro().getPortadaUrl(),
                r.getFechaReserva(), r.getFechaDisponible(), r.getFechaExpiracion(), r.getEstado());
    }
}
