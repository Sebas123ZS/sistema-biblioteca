package com.biblioteca.dto;

import com.biblioteca.model.Devolucion;
import com.biblioteca.model.Prestamo;
import com.biblioteca.model.enums.EstadoLibroDevuelto;

import java.time.LocalDate;

public record DevolucionResponse(
        Long id,
        String codigo,
        Long prestamoId,
        String prestamoCodigo,
        Long usuarioId,
        String usuarioNombre,
        Long libroId,
        String libroTitulo,
        LocalDate fechaPrestamo,
        LocalDate fechaVencimiento,
        LocalDate fechaDevolucion,
        Integer diasRetraso,
        EstadoLibroDevuelto estadoLibro,
        String observaciones,
        String reservaAsignadaA
) {
    public static DevolucionResponse from(Devolucion d, String reservaAsignadaA) {
        Prestamo p = d.getPrestamo();
        return new DevolucionResponse(d.getId(), String.format("DEV-%04d", d.getId()), p.getId(),
                String.format("PR-%04d", p.getId()), p.getUsuario().getId(), p.getUsuario().getNombreCompleto(),
                p.getLibro().getId(), p.getLibro().getTitulo(), p.getFechaPrestamo(), p.getFechaVencimiento(),
                d.getFechaDevolucion(), d.getDiasRetraso(), d.getEstadoLibro(), d.getObservaciones(), reservaAsignadaA);
    }

    public static DevolucionResponse from(Devolucion d) {
        return from(d, null);
    }
}
