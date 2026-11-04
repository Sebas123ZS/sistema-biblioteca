package com.biblioteca.dto;

import com.biblioteca.model.Prestamo;
import com.biblioteca.model.enums.EstadoPrestamo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record PrestamoResponse(
        Long id,
        String codigo,
        Long usuarioId,
        String usuarioNombre,
        String usuarioDocumento,
        Long libroId,
        String libroTitulo,
        String libroIsbn,
        String libroPortadaUrl,
        LocalDate fechaPrestamo,
        LocalDate fechaVencimiento,
        EstadoPrestamo estado,
        long diasRestantes,
        LocalDate fechaDevolucion
) {
    public static PrestamoResponse from(Prestamo p) {
        long dias = p.getEstado() == EstadoPrestamo.DEVUELTO ? 0
                : ChronoUnit.DAYS.between(LocalDate.now(), p.getFechaVencimiento());
        LocalDate fechaDev = p.getDevolucion() != null ? p.getDevolucion().getFechaDevolucion() : null;
        return new PrestamoResponse(p.getId(), String.format("PR-%04d", p.getId()),
                p.getUsuario().getId(), p.getUsuario().getNombreCompleto(), p.getUsuario().getDocumento(),
                p.getLibro().getId(), p.getLibro().getTitulo(), p.getLibro().getIsbn(), p.getLibro().getPortadaUrl(),
                p.getFechaPrestamo(), p.getFechaVencimiento(), p.getEstado(), dias, fechaDev);
    }
}
